package com.sprinthive.pokerhands.handrank;

import com.sprinthive.pokerhands.Card;
import com.sprinthive.pokerhands.CardRank;
import com.sprinthive.pokerhands.Suit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GoodPokerHandRanker implements HandRanker {

    public HandRank findBestHandRank(List<Card> cards) {
        if (cards.size() != 5) {
            return new NotRankableHandRanker(cards);
        }
        Collections.sort(cards);
        Collections.reverse(cards);
        try {
            if (testForRoyalFlush(cards)) {
                return new RoyalFlushHandRank(cards.get(0).getSuit());
            }
            if (testForStraightFlush(cards)) {
                return new StraightFlushHandRank(cards.get(0).getRank());
            }

            if (testForFourOfAKind(cards)) {
                return new FourOfAKindHandRank(getRank(cards, 3));
            }

            if (testForFullHouse(cards)) {
                return new FullHouseHandRank(getRank(cards, 2), getRank(cards, 1));
            }

            if (testForFlush(cards)) {
                return new FlushHandRank(cards);
            }

            if (testForStraight(cards)) {
                return new StraightHandRank(cards.get(0).getRank());
            }

            if (testForThreeOfAKind(cards)) {
                return new ThreeOfAKindHandRank(getRank(cards, 2));
            }

            if (testForTwoPair(cards)) {
                CardRank highCardRank = getRank(cards, 1);
                List<Card> restCards = cards.stream().filter(x -> x.getRank().compareTo(highCardRank) != 0).collect(Collectors.toList());
                CardRank lowCardRank = getRank(restCards, 1);

                restCards = cards.stream().filter(x -> x.getRank().compareTo(lowCardRank) != 0).collect(Collectors.toList());

                List<CardRank> cardRanks = new ArrayList<>();
                for (Card restCard : restCards) {
                    if (!cardRanks.contains(restCard.getRank())) {
                        cardRanks.add(restCard.getRank());
                    }
                }
                return new TwoPairHandRank(highCardRank, lowCardRank, restCards.get(0).getRank());
            }

            if (testForOnePair(cards)) {
                CardRank temp = getRank(cards, 1);
                List<Card> restCards = cards.stream().filter(x -> x.getRank().compareTo(temp) != 0).collect(Collectors.toList());
                List<CardRank> cardRanks = new ArrayList<>();
                for (Card restCard : restCards) {
                    if (!cardRanks.contains(restCard.getRank())) {
                        cardRanks.add(restCard.getRank());
                    }
                }
                return new OnePairHandRank(temp, cardRanks);
            }

            // High card
            return new HighCardHandRank(cards);
        } catch (Exception e) {
            return new NotRankableHandRanker(cards);
        }
    }

    public boolean testForStraight(List<Card> cards) {
        for (int i = 0; i < cards.size() - 1 ; i++) {
            Card a = cards.get(i);
            Card b = cards.get(i + 1);
            if (a.compareTo(b) != 1) {
                return false;
            }
        }
        return true;
    }
    public boolean testForFlush(List<Card> cards) {
        for (int i = 0; i < cards.size() - 1 ; i++) {
            Card a = cards.get(i);
            Card b = cards.get(i + 1);
            if (a.getSuit().compareTo(b.getSuit()) != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean testForStraightFlush(List<Card> cards) {
        return testForStraight(cards) && testForFlush(cards);
    }

    public boolean testForRoyalFlush(List<Card> cards) {
        // A royal flush is also a Straight flush - test for straight flush first
        if (!testForStraightFlush(cards)) {
            return false;
        }
        // test last card if Ace
        return cards.get(0).getRank().compareTo(CardRank.ACE) == 0;
    }

    public boolean testForOnePair(List<Card> cards) {
        for (int i = 0; i < cards.size() - 1; i++) {
            Card a = cards.get(i);
            Card b = cards.get(i + 1);

            if (a.getRank().compareTo(b.getRank()) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean testForTwoPair(List<Card> cards) {
        int pairs = 0;
        CardRank matchedCardRand = null;
        int i = 0;
        while (i < cards.size() - 1) {
            Card a = cards.get(i);
            Card b = cards.get(i + 1);
            if (a.getRank().compareTo(b.getRank()) == 0) {
                // We have a match - check if it's our first match
                // if it is not our first match check that we have a different card rand that we match on
                if (pairs == 0) {
                    pairs++;
                    matchedCardRand = a.getRank();
                } else if (a.getRank().compareTo(matchedCardRand) != 0) {
                    pairs++;
                }
                i++;
            }
            i++;
        }
        return pairs > 1;
    }

    public boolean testForThreeOfAKind(List<Card> cards) {
        for (int i = 0; i < cards.size() - 2; i++) {
            Card a = cards.get(i);
            Card b = cards.get(i + 1);
            Card c = cards.get(i + 2);
            if (a.getRank().compareTo(b.getRank()) == 0 && b.getRank().compareTo(c.getRank()) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean testForFourOfAKind(List<Card> cards) {
        int matchs = 0;
        CardRank matchedCardRank = null;
        int i = 0;
        while (i < cards.size() - 1) {
            Card a = cards.get(i);
            Card b = cards.get(i + 1);
            if (a.getRank().compareTo(b.getRank()) == 0) {
                // We have a pair - we need to check if that pair is not three of a kind
                if (matchedCardRank == null) {
                    matchs++;
                    matchedCardRank = a.getRank();
                } else
                    if (b.getRank().compareTo(matchedCardRank) == 0) {
                       matchs++;
                    }
            }
            i++;
        }
        return matchs > 2;
    }

    public boolean testForFullHouse(List<Card> cards) {
        boolean threeOfAKind = false;
        boolean twoOfAKind = false;
        int i = 0;
        while (i < cards.size() - 2) {
            Card a = cards.get(i);
            Card b = cards.get(i + 1);
            if (a.getRank().compareTo(b.getRank()) == 0) {
                // We have a pair - we need to check if that pair is not three of a kind
                Card c = cards.get(i + 2);
                if (b.getRank().compareTo(c.getRank()) == 0) {
                    // found a three of a kind
                    threeOfAKind = true;
                    i++;
                } else {
                    twoOfAKind = true;
                }
                i++;
            }
            i++;
        }
        return threeOfAKind && twoOfAKind;
    }

    private CardRank getRank(List<Card> cards, int numberOfMatch) throws Exception {
        int i = 0;
        while (i < cards.size() - 1) {
            int matches = 0;
            int j = i + 1;
            Card temp = cards.get(i);
            while(j < cards.size() && temp.getRank().compareTo(cards.get(j).getRank()) == 0) {
                matches++;
                j++;
            }

            if (matches == numberOfMatch) {
                return cards.get(i).getRank();
            }

            i++;
        }
        throw new Exception("Cannot execute");
    }
}
