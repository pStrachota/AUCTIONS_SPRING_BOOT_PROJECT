package pl.lodz.p.pstrachota.auctions_spring_boot_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.dto.BidRequest;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.model.Bid;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.service.impl.BidServiceImpl;

@RestController
@RequiredArgsConstructor
public class BidController {

    private final BidServiceImpl bidServiceImpl;

    @Operation(summary = "Create new bid")
    @PostMapping("/auctions/{id}/bids")
    public ResponseEntity<Bid> createBid(@PathVariable Long id,
                                         @RequestBody @Valid BidRequest bidRequest) {

        return new ResponseEntity<>(bidServiceImpl.createBid(bidRequest, id),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Delete bid by id")
    @DeleteMapping("/auctions/{id}/bids/{bidId}")
    public ResponseEntity<Bid> deleteBid(@PathVariable Long id,
                                         @PathVariable Long bidId) {

        return new ResponseEntity<>(bidServiceImpl.deleteBid(id, bidId), HttpStatus.OK);
    }

}
