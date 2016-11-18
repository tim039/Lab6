package pokerBase;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import pokerEnums.eCardDestination;
import pokerEnums.eCardNo;
import pokerEnums.eDrawCount;
import pokerEnums.eGame;
import pokerEnums.eHandExceptionType;
import pokerEnums.eHandStrength;
import pokerEnums.eRank;
import pokerEnums.eSuit;
import exceptions.HandException;
import org.apache.commons.math3.util.CombinatoricsUtils;

public class Hand implements Serializable {

	private UUID HandId;
	private Player HandPlayer;
	private ArrayList<Card> CardsInHand = new ArrayList<Card>();
	private boolean bScored;
	private HandScore hs;
	private ArrayList<Hand> ExplodedHands = new ArrayList<Hand>();
	private boolean bHandFolded = false;
	private boolean bIsHandWinner = false;

	

	public Hand(Player handPlayer, UUID HandID) {
		
		this.HandId = (HandID == null) ? UUID.randomUUID() : HandID;		
		this.HandPlayer = handPlayer;
	}	
	
	 public UUID getHandId() {
		return HandId;
	}

	public Player getHandPlayer() {
		return HandPlayer;
	}


	public void setHandPlayer(Player handPlayer) {
		HandPlayer = handPlayer;
	}


	public ArrayList<Card> getCardsInHand() {
		return CardsInHand;
	}

	public void AddToCardsInHand(Card c) {
		CardsInHand.add(c);
	}

	public boolean isbScored() {
		return bScored;
	}

	public HandScore getHandScore() {
		return hs;
	}

	public boolean isbIsHandWinner() {
		return bIsHandWinner;
	}

	public void setbIsHandWinner(boolean bIsHandWinner) {
		this.bIsHandWinner = bIsHandWinner;
	}
	
	public static Hand PickHandFromCombination(Player p, Hand PlayerHand, Hand CommonHand, GamePlay gme) throws HandException {

		ArrayList<Hand> CombinHands = new ArrayList<Hand>();
		int iPlayerNumberOfCards = gme.getRule().GetPlayerNumberOfCards();
		int iPlayerCardsMin = gme.getRule().getPlayerCardsMin();
		int iPlayerCardsMax = gme.getRule().getPlayerCardsMax();
		int iCommonCardsMin = gme.getRule().getCommunityCardsMin();
		int iCommonCardsMax = gme.getRule().getCommunityCardsMax();
		
		
		for (int iPassPlayer = 0; iPassPlayer <= (iPlayerCardsMax - iPlayerCardsMin);iPassPlayer++)
		{
			Iterator iterPlayer = CombinatoricsUtils.combinationsIterator(iPlayerNumberOfCards, (iPlayerCardsMin + iPassPlayer));
			while (iterPlayer.hasNext())				
			{
				int[] iPlayerCardsToPick = (int[]) iterPlayer.next();

				if (iCommonCardsMax > 0)
				{
					Iterator iterCommon = CombinatoricsUtils.combinationsIterator(iCommonCardsMax, (iCommonCardsMax - iPlayerCardsToPick.length));
					while (iterCommon.hasNext())
					{
						int[] iCommonCardsToPick = (int[]) iterCommon.next();
						Hand h = new Hand(p, PlayerHand.getHandId());
						for (int iPlayerArrayPos = 0; iPlayerArrayPos < iPlayerCardsToPick.length; iPlayerArrayPos++)
						{						
							h.AddToCardsInHand((Card) PlayerHand.getCardsInHand().get(iPlayerCardsToPick[iPlayerArrayPos]));
						}
						
						for (int iCommonArrayPos = 0; iCommonArrayPos < iCommonCardsToPick.length; iCommonArrayPos++)
						{
							h.AddToCardsInHand((Card) CommonHand.getCardsInHand().get(iCommonCardsToPick[iCommonArrayPos]));
						}				
						CombinHands.add(h);
					}						
				}
				else if (iCommonCardsMax == 0)
				{
					Hand h = new Hand(p, PlayerHand.getHandId());
					for (int iPlayerArrayPos = 0; iPlayerArrayPos < iPlayerCardsToPick.length; iPlayerArrayPos++)
					{
						h.AddToCardsInHand((Card) PlayerHand.getCardsInHand().get(iPlayerCardsToPick[iPlayerArrayPos]));
					}
					CombinHands.add(h);
				}
			}			
		}

		//	Evaluate each hand (why not?)
		ArrayList<Hand> ScoredHands = new ArrayList<Hand>();
		for (Hand h : CombinHands) {
			try {
				h = Hand.EvaluateHand(h);
				ScoredHands.add(h);
			} catch (HandException e) {
				System.out.println("Exception thrown");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return PickBestHand(ScoredHands);		
	}

	public ArrayList<Card> GetCardsDrawn(eDrawCount eDrawCount, eGame eGame, eCardDestination eCardDestination)
	{
		int iStartCard = 0;
		int iStartCardCommon = 0;
		int iCardCount = 0;
		Rule rle = new Rule(eGame);
		ArrayList<Card> CardsDrawn = new ArrayList<Card>();
		
		for (eDrawCount eDraw : eDrawCount.values()) {			
			iCardCount= rle.GetDrawCard(eDraw) != null ?  rle.GetDrawCard(eDraw).getCardCount().getCardCount() : 0;			
			if (eDraw == eDrawCount)
			{
				break;
			}
			if (rle.GetDrawCard(eDraw) != null)
			{
				if (rle.GetDrawCard(eDraw).getCardDestination()  == eCardDestination.Community)
				{
					iStartCardCommon += rle.GetDrawCard(eDraw).getCardCount().getCardCount();
				}
				else if  (rle.GetDrawCard(eDraw).getCardDestination() == eCardDestination.Player)
				{
					iStartCard += rle.GetDrawCard(eDraw).getCardCount().getCardCount();
				}
			}
		}		
		
		for (int iCard = 0;iCard < iCardCount;iCard++)
		{
			if (eCardDestination == eCardDestination.Community)
			{
				CardsDrawn.add(CardsInHand.get(iCard + iStartCardCommon));
			}
			else if  (eCardDestination == eCardDestination.Player)
			{				
				CardsDrawn.add(CardsInHand.get(iCard + iStartCard));	
			}			
		}		
		return CardsDrawn;
	}
	
	public void EvaluateHand() {
		try {
			Hand h = EvaluateHand(this);
			hs = h.getHandScore();
			bScored = h.bScored;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * <b>EvaluateHand</b> is a static method that will score a given Hand of
	 * cards
	 * 
	 * @param h
	 * @return
	 * @throws HandException
	 */

	static Hand EvaluateHand(Hand h) throws HandException {

		Collections.sort(h.getCardsInHand());

		if (h.getCardsInHand().size() != 5) {
			throw new HandException(h, eHandExceptionType.ShortHand);
		}

		ArrayList<Hand> ExplodedHands = new ArrayList<Hand>();

		ExplodedHands = ExplodeHands(h, h.getHandPlayer());

		for (Hand hEval : ExplodedHands) {

			if (hEval.getCardsInHand().size() != 5) {
				throw new HandException(h, eHandExceptionType.ShortHand);
			}
			Collections.sort(hEval.getCardsInHand());

			HandScore hs = new HandScore();
			try {
				Class<?> c = Class.forName("pokerBase.Hand");

				for (eHandStrength hstr : eHandStrength.values()) {
					Class[] cArg = new Class[2];
					cArg[0] = pokerBase.Hand.class;
					cArg[1] = pokerBase.HandScore.class;

					Method meth = c.getMethod(hstr.getEvalMethod(), cArg);
					Object o = meth.invoke(null, new Object[] { hEval, hs });

					if ((Boolean) o) {
						break;
					}
				}

				hEval.bScored = true;
				hEval.hs = hs;

			} catch (ClassNotFoundException x) {
				x.printStackTrace();
			} catch (IllegalAccessException x) {
				x.printStackTrace();
			} catch (NoSuchMethodException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		Collections.sort(ExplodedHands, Hand.HandRank);
		h.ExplodedHands = ExplodedHands;
		return ExplodedHands.get(0);

	}

	public static Hand PickBestHand(ArrayList<Hand> Hands) throws HandException {
		Collections.sort(Hands, Hand.HandRank);
		if (Hand.HandRank.compare(Hands.get(0), Hands.get(1)) == 0) {
			throw new HandException(Hands.get(0), eHandExceptionType.TieHand);
		} else {
			return Hands.get(0);
		}
	}

	/**
	 * 
	 * @param h
	 * @param hs
	 * @return
	 */
	public  static ArrayList<Hand> ExplodeHands(Hand h, Player p) {

        ArrayList<Hand> ReturnHands = new ArrayList<Hand>();
        ReturnHands.add(h);
        for (int iCard = 0; iCard < 5; iCard++) {
            ReturnHands = SubstituteCard(iCard, ReturnHands, p);
        }
        return ReturnHands;
    }
	private static ArrayList<Hand> SubstituteCard(int iCardSub, ArrayList<Hand> hands, Player p) {
        ArrayList<Hand> CreatedHands = new ArrayList<Hand>();
        Deck CreatedDeck = new Deck();

        for (Hand h : hands) {
            if ((h.getCardsInHand().get(iCardSub).isWild() == true)
                    || (h.getCardsInHand().get(iCardSub).geteSuit() == eSuit.JOKER)) {
                for (Card JokerDeckCard : CreatedDeck.getDeckCards()) {
                    Hand CreatedHand = new Hand(p, h.getHandId());
                    for (int iCard = 0; iCard < 5; iCard++) {
                        if (iCardSub == iCard) {
                            CreatedHand.AddToCardsInHand(JokerDeckCard);
                        } else {
                            CreatedHand.AddToCardsInHand(h.getCardsInHand().get(iCard));
                        }
                    }
                    CreatedHands.add(CreatedHand);
                }
            } else {
                Hand CreatedHand = new Hand(p, h.getHandId());
                for (int iCard = 0; iCard < 5; iCard++) {
                    CreatedHand.AddToCardsInHand(h.getCardsInHand().get(iCard));
                }
                CreatedHands.add(CreatedHand);
            }
        }

        return CreatedHands;
    }


	public static boolean isHandRoyalFlush(Hand h, HandScore hs) {

		Card c = new Card();
		boolean isRoyalFlush = false;
		if ((isHandFlush(h.getCardsInHand())) && (isStraight(h.getCardsInHand(), c))) {
			if (c.geteRank() == eRank.ACE) {
				isRoyalFlush = true;
				hs.setHandStrength(eHandStrength.RoyalFlush.getHandStrength());
				hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr());
				hs.setLoHand(0);
			}
		}
		return isRoyalFlush;
	}

	/**
	 * isHandStraightFlush - Will return true if the hand is a straight flush
	 * 
	 * @param h
	 * @param hs
	 * @return
	 */
	public static boolean isHandStraightFlush(Hand h, HandScore hs) {
		Card c = new Card();
		boolean isHandStraightFlush = false;
		if ((isHandFlush(h.getCardsInHand())) && (isStraight(h.getCardsInHand(), c))) {
			isHandStraightFlush = true;
			hs.setHandStrength(eHandStrength.StraightFlush.getHandStrength());
			hs.setHiHand(c.geteRank().getiRankNbr());
			hs.setLoHand(0);
		}

		return isHandStraightFlush;
}

	/**
	 * isHandFiveOfAKind - this method will determine if the hand is a five of a
	 * kind
	 * 
	 * @param h
	 * @param hs
	 * @return
	 */
	public static boolean isHandFiveOfAKind(Hand h, HandScore hs) {

		boolean bHandCheck = false;

		if (h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FifthCard.getCardNo()).geteRank()) {
			bHandCheck = true;
			hs.setHandStrength(eHandStrength.FiveOfAKind.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(0);
			ArrayList<Card> kickers = new ArrayList<Card>();
			hs.setKickers(kickers);

		}
		return bHandCheck;
	}

	/**
	 * isHandFourOfAKind - this method will determine if the hand is a four of a
	 * kind
	 * 
	 * @param h
	 * @param hs
	 * @return
	 */
	public static boolean isHandFourOfAKind(Hand h, HandScore hs) {

		boolean bHandCheck = false;

		if (h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FourthCard.getCardNo()).geteRank()) {
			bHandCheck = true;
			hs.setHandStrength(eHandStrength.FourOfAKind.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(0);
			ArrayList<Card> kickers = new ArrayList<Card>();
			kickers.add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));
			hs.setKickers(kickers);

		} else if (h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FifthCard.getCardNo()).geteRank()) {
			bHandCheck = true;
			hs.setHandStrength(eHandStrength.FourOfAKind.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(0);
			ArrayList<Card> kickers = new ArrayList<Card>();
			kickers.add(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()));
			hs.setKickers(kickers);
		}

		return bHandCheck;
	}

	/**
	 * isHandFullHouse - This method will determine if the hand is a full house
	 * 
	 * @param h
	 * @param hs
	 * @return
	 */
	public static boolean isHandFullHouse(Hand h, HandScore hs) {

		boolean isFullHouse = false;
		ArrayList<Card> kickers = new ArrayList<Card>();
		if ((h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.ThirdCard.getCardNo()).geteRank())
				&& (h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank() == h.getCardsInHand()
						.get(eCardNo.FifthCard.getCardNo()).geteRank())) {
			isFullHouse = true;
			hs.setHandStrength(eHandStrength.FullHouse.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank().getiRankNbr());
		} else if ((h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.SecondCard.getCardNo()).geteRank())
				&& (h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank() == h.getCardsInHand()
						.get(eCardNo.FifthCard.getCardNo()).geteRank())) {
			isFullHouse = true;
			hs.setHandStrength(eHandStrength.FullHouse.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr());
		}

		return isFullHouse;

	}

	public static boolean isHandFlush(Hand h, HandScore hs) {

		boolean bIsFlush = false;
		if (isHandFlush(h.getCardsInHand())) {
			hs.setHandStrength(eHandStrength.Flush.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(0);
			ArrayList<Card> kickers = new ArrayList<Card>();
			kickers.add(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));
			hs.setKickers(kickers);
			bIsFlush = true;
		}

		return bIsFlush;
	}

	public static boolean isHandFlush(ArrayList<Card> cards) {
		int cnt = 0;
		boolean bIsFlush = false;
		for (eSuit Suit : eSuit.values()) {
			cnt = 0;
			for (Card c : cards) {
				if (c.geteSuit() == Suit) {
					cnt++;
				}
			}
			if (cnt == 5)
				bIsFlush = true;

		}
		return bIsFlush;
	}

	public static boolean isStraight(ArrayList<Card> cards, Card highCard) {
		boolean bIsStraight = false;
		boolean bAce = false;

		int iStartCard = 0;
		highCard.seteRank(cards.get(eCardNo.FirstCard.getCardNo()).geteRank());
		highCard.seteSuit(cards.get(eCardNo.FirstCard.getCardNo()).geteSuit());

		if (cards.get(eCardNo.FirstCard.getCardNo()).geteRank() == eRank.ACE) {
			bAce = true;
			iStartCard++;
		}

		for (int a = iStartCard; a < cards.size() - 1; a++) {
			if ((cards.get(a).geteRank().getiRankNbr() - cards.get(a + 1).geteRank().getiRankNbr()) == 1) {
				bIsStraight = true;
			} else {
				bIsStraight = false;
				break;
			}
		}

		if ((bAce) && (bIsStraight)) {
			if (cards.get(eCardNo.SecondCard.getCardNo()).geteRank() == eRank.KING) {
				highCard.seteRank(cards.get(eCardNo.FirstCard.getCardNo()).geteRank());
				highCard.seteSuit(cards.get(eCardNo.FirstCard.getCardNo()).geteSuit());
			} else if (cards.get(eCardNo.SecondCard.getCardNo()).geteRank() == eRank.FIVE) {
				highCard.seteRank(cards.get(eCardNo.SecondCard.getCardNo()).geteRank());
				highCard.seteSuit(cards.get(eCardNo.SecondCard.getCardNo()).geteSuit());
			} else {
				bIsStraight = false;
			}
		}
		return bIsStraight;
	}

	public static boolean isHandStraight(Hand h, HandScore hs) {

		boolean bIsStraight = false;
		Card highCard = new Card();
		if (isStraight(h.getCardsInHand(), highCard)) {
			hs.setHandStrength(eHandStrength.Straight.getHandStrength());
			hs.setHiHand(highCard.geteRank().getiRankNbr());
			hs.setLoHand(0);
			bIsStraight = true;
		}
		return bIsStraight;
	}

	public static boolean isHandThreeOfAKind(Hand h, HandScore hs) {

		boolean isThreeOfAKind = false;
		ArrayList<Card> kickers = new ArrayList<Card>();
		if (h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.ThirdCard.getCardNo()).geteRank()) {
			isThreeOfAKind = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr());
			kickers.add(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));
		} else if (h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FourthCard.getCardNo()).geteRank()) {
			isThreeOfAKind = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank().getiRankNbr());
			kickers.add(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));

		} else if (h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FifthCard.getCardNo()).geteRank()) {
			isThreeOfAKind = true;
			hs.setHiHand(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank().getiRankNbr());
			kickers.add(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()));
			kickers.add(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()));

		}

		if (isThreeOfAKind) {
			hs.setHandStrength(eHandStrength.ThreeOfAKind.getHandStrength());
			hs.setLoHand(0);
			hs.setKickers(kickers);
		}

		return isThreeOfAKind;
	}

	public static boolean isHandTwoPair(Hand h, HandScore hs) {

		boolean isTwoPair = false;
		ArrayList<Card> kickers = new ArrayList<Card>();
		if ((h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.SecondCard.getCardNo()).geteRank())
				&& (h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank() == h.getCardsInHand()
						.get(eCardNo.FourthCard.getCardNo()).geteRank())) {
			isTwoPair = true;
			hs.setHandStrength(eHandStrength.TwoPair.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank().getiRankNbr());
			kickers.add(h.getCardsInHand().get((eCardNo.FifthCard.getCardNo())));
			hs.setKickers(kickers);
		} else if ((h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.SecondCard.getCardNo()).geteRank())
				&& (h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank() == h.getCardsInHand()
						.get(eCardNo.FifthCard.getCardNo()).geteRank())) {
			isTwoPair = true;
			hs.setHandStrength(eHandStrength.TwoPair.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank().getiRankNbr());
			kickers.add(h.getCardsInHand().get((eCardNo.ThirdCard.getCardNo())));
			hs.setKickers(kickers);
		} else if ((h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.ThirdCard.getCardNo()).geteRank())
				&& (h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank() == h.getCardsInHand()
						.get(eCardNo.FifthCard.getCardNo()).geteRank())) {
			isTwoPair = true;
			hs.setHandStrength(eHandStrength.TwoPair.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank().getiRankNbr());
			kickers.add(h.getCardsInHand().get((eCardNo.FirstCard.getCardNo())));
			hs.setKickers(kickers);
		}
		return isTwoPair;
	}

	public static boolean isHandPair(Hand h, HandScore hs) {
		boolean isPair = false;
		ArrayList<Card> kickers = new ArrayList<Card>();
		if (h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.SecondCard.getCardNo()).geteRank()) {
			isPair = true;
			hs.setHandStrength(eHandStrength.Pair.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(0);
			kickers.add(h.getCardsInHand().get((eCardNo.ThirdCard.getCardNo())));
			kickers.add(h.getCardsInHand().get((eCardNo.FourthCard.getCardNo())));
			kickers.add(h.getCardsInHand().get((eCardNo.FifthCard.getCardNo())));
			hs.setKickers(kickers);
		} else if (h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.ThirdCard.getCardNo()).geteRank()) {
			isPair = true;
			hs.setHandStrength(eHandStrength.Pair.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(0);
			kickers.add(h.getCardsInHand().get((eCardNo.FirstCard.getCardNo())));
			kickers.add(h.getCardsInHand().get((eCardNo.FourthCard.getCardNo())));
			kickers.add(h.getCardsInHand().get((eCardNo.FifthCard.getCardNo())));
			hs.setKickers(kickers);
		} else if (h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FourthCard.getCardNo()).geteRank()) {
			isPair = true;
			hs.setHandStrength(eHandStrength.Pair.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(0);
			kickers.add(h.getCardsInHand().get((eCardNo.FirstCard.getCardNo())));
			kickers.add(h.getCardsInHand().get((eCardNo.SecondCard.getCardNo())));
			kickers.add(h.getCardsInHand().get((eCardNo.FifthCard.getCardNo())));
			hs.setKickers(kickers);
		} else if (h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank() == h.getCardsInHand()
				.get(eCardNo.FifthCard.getCardNo()).geteRank()) {
			isPair = true;
			hs.setHandStrength(eHandStrength.Pair.getHandStrength());
			hs.setHiHand(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()).geteRank().getiRankNbr());
			hs.setLoHand(0);
			kickers.add(h.getCardsInHand().get((eCardNo.FirstCard.getCardNo())));
			kickers.add(h.getCardsInHand().get((eCardNo.SecondCard.getCardNo())));
			kickers.add(h.getCardsInHand().get((eCardNo.ThirdCard.getCardNo())));
			hs.setKickers(kickers);
		}
		return isPair;
	}

	public static boolean isHandHighCard(Hand h, HandScore hs) {
		hs.setHandStrength(eHandStrength.HighCard.getHandStrength());
		hs.setHiHand(h.getCardsInHand().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr());
		hs.setLoHand(0);
		ArrayList<Card> kickers = new ArrayList<Card>();
		kickers.add(h.getCardsInHand().get(eCardNo.SecondCard.getCardNo()));
		kickers.add(h.getCardsInHand().get(eCardNo.ThirdCard.getCardNo()));
		kickers.add(h.getCardsInHand().get(eCardNo.FourthCard.getCardNo()));
		kickers.add(h.getCardsInHand().get(eCardNo.FifthCard.getCardNo()));
		hs.setKickers(kickers);
		return true;
	}
	
    public boolean isbHandFolded() {
		return bHandFolded;
	}

	public void setbHandFolded(boolean bHandFolded) {
		this.bHandFolded = bHandFolded;
	}

	public static Comparator<Hand> HandRank = new Comparator<Hand>() {

        public int compare(Hand h1, Hand h2) {

            int result = 0;

            result = h2.getHandScore().getHandStrength() - h1.getHandScore().getHandStrength();

            if (result != 0) {
                return result;
            }

            result = h2.getHandScore().getHiHand() - h1.getHandScore().getHiHand();
            if (result != 0) {
                return result;
            }

            result = h2.getHandScore().getLoHand() - h1.getHandScore().getLoHand();
            if (result != 0) {
                return result;
            }

            if (h2.getHandScore().getKickers().size() > 0) {
                if (h1.getHandScore().getKickers().size() > 0) {
                    result = h2.getHandScore().getKickers().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr()
                            - h1.getHandScore().getKickers().get(eCardNo.FirstCard.getCardNo()).geteRank().getiRankNbr();
                }
                if (result != 0) {
                    return result;
                }
            }

            if (h2.getHandScore().getKickers().size() > 1) {
                if (h1.getHandScore().getKickers().size() > 1) {
                    result = h2.getHandScore().getKickers().get(eCardNo.SecondCard.getCardNo()).geteRank().getiRankNbr()
                            - h1.getHandScore().getKickers().get(eCardNo.SecondCard.getCardNo()).geteRank().getiRankNbr();
                }
                if (result != 0) {
                    return result;
                }
            }

            if (h2.getHandScore().getKickers().size() > 2) {
                if (h1.getHandScore().getKickers().size() > 2) {
                    result = h2.getHandScore().getKickers().get(eCardNo.ThirdCard.getCardNo()).geteRank().getiRankNbr()
                            - h1.getHandScore().getKickers().get(eCardNo.ThirdCard.getCardNo()).geteRank().getiRankNbr();
                }
                if (result != 0) {
                    return result;
                }
            }

            if (h2.getHandScore().getKickers().size() > 3) {
                if (h1.getHandScore().getKickers().size() > 3) {
                    result = h2.getHandScore().getKickers().get(eCardNo.FourthCard.getCardNo()).geteRank().getiRankNbr()
                            - h1.getHandScore().getKickers().get(eCardNo.FourthCard.getCardNo()).geteRank().getiRankNbr();
                }
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }
    };
}