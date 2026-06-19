package by.radeflex.steamshop.dto;

public interface ProductInfo {
    String title();
    String description();
    Integer price();

    default String getTitle() {return title();}
    default String getDescription() {return description();}
    default Integer getPrice() {return price();}
}
