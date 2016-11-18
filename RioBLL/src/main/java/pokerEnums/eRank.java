package pokerEnums;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

public enum eRank {
	
	TWO(2) {
		@Override
		public String toString() {
			return "Two ";
		}
	}, 
	THREE(3) {
		@Override
		public String toString() {
			return "Three ";
		}
	},  
	FOUR(4) {
		@Override
		public String toString() {
			return "Four ";
		}
	}, 
	FIVE(5) {
		@Override
		public String toString() {
			return "Five";
		}
	}, 
	SIX(6) {
		@Override
		public String toString() {
			return "Six";
		}
	}, 
	SEVEN(7) {
		@Override
		public String toString() {
			return "Seven";
		}
	},  
	EIGHT(8) {
		@Override
		public String toString() {
			return "Eight";
		}
	}, 
	NINE(9) {
		@Override
		public String toString() {
			return "Nine";
		}
	}, 
	TEN(10) {
		@Override
		public String toString() {
			return "Ten";
		}
	}, 
	JACK(11) {
		@Override
		public String toString() {
			return "Jack";
		}
	}, 
	QUEEN(12) {
		@Override
		public String toString() {
			return "Queen";
		}
	}, 
	KING(13) {
		@Override
		public String toString() {
			return "King";
		}
	}, 
	ACE(14) {
		@Override
		public String toString() {
			return "Ace";
		}
	}, 
	JOKER(99) {
		@Override
		public String toString() {
			return "JOKER";
		}
	};

	private int iRankNbr;

	private eRank(int iRankNbr) {
		this.iRankNbr = iRankNbr;
	}

	@XmlElement
	public int getiRankNbr() {
		return iRankNbr;
	}
	
	private static Map<Integer, eRank> map = new HashMap<Integer, eRank>();

	static {
		for (eRank rank : eRank.values()) {
			map.put(rank.getiRankNbr(), rank);
		}
	}

	public static eRank geteRank(int iRank) {
		return map.get(iRank);
	}


	
}
