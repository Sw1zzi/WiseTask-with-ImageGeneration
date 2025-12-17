package ru.spb.ipo.generator.image_generator.cdsl.model;



import java.util.Objects;



public class Card {

    private final String rank;

    private final String suit;



    public Card(String rank, String suit) {

        this.rank = rank != null ? rank.toUpperCase() : null;

        this.suit = suit != null ? suit.toUpperCase() : null;

    }



    public String getRank() { return rank; }

    public String getSuit() { return suit; }



    @Override

    public String toString() {

        return rank + " of " + suit;

    }



    @Override

    public boolean equals(Object obj) {

        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        Card card = (Card) obj;

        return Objects.equals(rank, card.rank) && Objects.equals(suit, card.suit);

    }



    @Override

    public int hashCode() {

        return Objects.hash(rank, suit);

    }

}