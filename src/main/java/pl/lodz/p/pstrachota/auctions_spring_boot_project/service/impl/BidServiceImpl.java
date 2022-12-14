package pl.lodz.p.pstrachota.auctions_spring_boot_project.service.impl;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.dto.BidRequest;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.events.MailSenderPublisher;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.exceptions.IncorrectAuctionTypeException;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.exceptions.IncorrectDateException;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.exceptions.IncorrectOperationException;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.exceptions.IncorrectPriceException;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.exceptions.NotFoundException;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.model.Auction;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.model.AuctionType;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.model.Bid;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.repository.AuctionRepository;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.repository.BidRepository;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.service.interfaces.BidService;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.service.mapper.BidDtoMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final MailSenderPublisher mailSenderPublisher;

    public Bid createBid(BidRequest bidRequest, Long auctionId) {
        Bid bid = BidDtoMapper.mapToBid(bidRequest, auctionId);
        long relatedOfferId = bid.getRelatedOfferId();

        Bid savedBid = null;

        Auction auction = auctionRepository.findById(relatedOfferId).orElseThrow(
                () -> new NotFoundException(
                        "Offer with id " + relatedOfferId + " not found"));

        if (auction.getAuctionType().equals(AuctionType.BUY_NOW)) {
            throw new IncorrectAuctionTypeException("Cannot bid on buy now auction");
        }
        if (bid.getBidPrice().compareTo(auction.getCurrentPrice()) <= 0) {
            throw new IncorrectPriceException("Bid price must be greater than offer price");
        }
        if (LocalDateTime.now().isAfter(auction.getAuctionEndTime())) {
            throw new IncorrectDateException("Auction has ended");
        }

        auction.setCurrentPrice(bid.getBidPrice());
        auction.setAuctionType(AuctionType.BIDDING);
        auctionRepository.save(auction);
        savedBid = bidRepository.save(bid);


        List<Bid> bidsForGivenOffer = bidRepository.findByRelatedOfferId(relatedOfferId);
        List<String> emailBids =
                bidsForGivenOffer.stream().map(Bid::getEmail).collect(Collectors.toList());
        mailSenderPublisher.publishNewBid(emailBids, relatedOfferId, bid.getBidPrice());
        return savedBid;
    }

    @Override
    public Bid deleteBid(Long auctionId, Long bidId) {

        Bid bidToDelete = bidRepository.findById(bidId).orElseThrow(
                () -> new NotFoundException("Bid with id " + bidId + " not found"));
        List<Bid> bidsForGivenOffer = bidRepository.findByRelatedOfferId(auctionId);
        Bid highestPriceBid =
                bidsForGivenOffer.stream().max(Comparator.comparing(Bid::getBidPrice)).get();

        if (!bidToDelete.getEmail().equals(highestPriceBid.getEmail())) {
            throw new IncorrectOperationException("You can only delete your own bid");
        }

        if (highestPriceBid.getId() != bidId) {
            throw new IncorrectOperationException("You can only delete bid with highest price");
        }

        Auction auctionToChangePrice = auctionRepository.findById(auctionId).orElseThrow(
                () -> new NotFoundException("Auction with id " + auctionId + " not found"));

        if (bidsForGivenOffer.size() == 1) {
            auctionToChangePrice.setCurrentPrice(auctionToChangePrice.getStartingPrice());
        } else {
            auctionToChangePrice.setCurrentPrice(highestPriceBid.getBidPrice());
        }

        List<String> emailBids =
                bidsForGivenOffer.stream().map(Bid::getEmail).collect(Collectors.toList());

        mailSenderPublisher.publishDeletedBid(emailBids, auctionId, highestPriceBid.getBidPrice());
        bidRepository.delete(bidToDelete);
        return bidToDelete;
    }


}
