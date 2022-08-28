package pl.lodz.p.pstrachota.auctions_spring_boot_project.service.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import pl.lodz.p.pstrachota.auctions_spring_boot_project.model.ItemCategory;

@Converter
public class ItemCategoryConverter implements AttributeConverter<ItemCategory, String> {
    @Override
    public String convertToDatabaseColumn(ItemCategory ItemCategory) {
        if (ItemCategory == null) {
            return null;
        }

        String itemCategoryString = null;

        switch (ItemCategory) {
            case BOOK -> itemCategoryString = "BOOK";
            case CLOTHES -> itemCategoryString = "CLOTHES";
            case ELECTRONICS -> itemCategoryString = "ELECTRONICS";
            case FURNITURE -> itemCategoryString = "FURNITURE";
            case OTHER -> itemCategoryString = "OTHER";
        }
        return itemCategoryString;
    }

    @Override
    public ItemCategory convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }

        ItemCategory itemCategory = null;

        switch (s) {
            case "BOOK" -> itemCategory = ItemCategory.BOOK;
            case "CLOTHES" -> itemCategory = ItemCategory.CLOTHES;
            case "ELECTRONICS" -> itemCategory = ItemCategory.ELECTRONICS;
            case "FURNITURE" -> itemCategory = ItemCategory.FURNITURE;
            case "OTHER" -> itemCategory = ItemCategory.OTHER;
        }

        return itemCategory;
    }
}