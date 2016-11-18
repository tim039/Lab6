package poker.app.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import exceptions.DeckException;
import exceptions.HandException;
import netgame.common.Hub;
import pokerBase.Action;
import pokerBase.Card;
import pokerBase.CardDraw;
import pokerBase.Deck;
import pokerBase.GamePlay;
import pokerBase.GamePlayPlayerHand;
import pokerBase.Player;
import pokerBase.Rule;
import pokerBase.Table;
import pokerEnums.eAction;
import pokerEnums.eCardDestination;
import pokerEnums.eDeckExceptionType;
import pokerEnums.eDrawCount;
import pokerEnums.eGame;
import pokerEnums.eGameState;

public class PokerHub extends Hub {

	private Table HubPokerTable = new Table();
	private GamePlay HubGamePlay;
	private int iDealNbr = 0;

	public PokerHub(int port) throws IOException {
		super(port);
	}

	protected void playerConnected(int playerID) {

		if (playerID == 2) {
			shutdownServerSocket();
		}
	}

	protected void playerDisconnected(int playerID) {
		shutDownHub();
	}

	protected void messageReceived(int ClientID, Object message) {

		if (message instanceof Action) {
			Player actPlayer = (Player) ((Action) message).getPlayer();
			Action act = (Action) message;
			switch (act.getAction()) {
			case Sit:
				HubPokerTable.AddPlayerToTable(actPlayer);
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case Leave:
				HubPokerTable.RemovePlayerFromTable(actPlayer);
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case TableState:
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case StartGame:
				// Get the rule from the Action object.
				Rule rle = new Rule(act.geteGame());
				// Start the new instance of GamePlay
				Player pDealer = HubPokerTable.PickRandomPlayerAtTable();

				HubGamePlay = new GamePlay(rle, pDealer.getPlayerID());
				// Add Players to Game
				HubGamePlay.setGamePlayers(HubPokerTable.getHashPlayers());
				// Set the order of players
				HubGamePlay.setiActOrder(GamePlay.GetOrder(pDealer.getiPlayerPosition()));


			case Draw:

				HubGamePlay
						.seteDrawCountLast(eDrawCount.geteDrawCount(HubGamePlay.geteDrawCountLast().getDrawNo() + 1));
				HubGamePlay.seteGameState(eGameState.DRAW);
				CardDraw cd = HubGamePlay.getRule().GetDrawCard(HubGamePlay.geteDrawCountLast());
				int iCardsToDraw = cd.getCardCount().getCardCount();

				if (cd.getCardDestination() == eCardDestination.Player) {
					for (int i : HubGamePlay.getiActOrder()) {
						Player p = HubGamePlay.getPlayerByPosition(i);
						if (p != null) {
							for (int iDraw = 0; iDraw < iCardsToDraw; iDraw++) {
								try {
									HubGamePlay.drawCard(p, cd.getCardDestination());
								} catch (DeckException e) {
									// Whoops! Exception was throw... send it
									// back to the client
									resetOutput();
									sendToAll(e);
									e.printStackTrace();
									return;
								}
							}
						}
					}
				} else if (cd.getCardDestination() == eCardDestination.Community) {
					System.out.println("Community");
					Player p = HubGamePlay.getPlayerCommon();
					if (p != null) {
						for (int iDraw = 0; iDraw < iCardsToDraw; iDraw++) {
							try {
								HubGamePlay.drawCard(p, cd.getCardDestination());
							} catch (DeckException e) {
								// Whoops! Exception was throw... send it
								// back to the client
								resetOutput();
								sendToAll(e);
								e.printStackTrace();
								return;
							}
						}
					}
				}

				HubGamePlay.isGameOver();
				
				resetOutput();
				sendToAll(HubGamePlay);
				break;
			case ScoreGame:
				// Am I at the end of the game?
				try {
					HubGamePlay.ScoreGame();
					HubGamePlay.seteGameState(eGameState.SCORED);
				} catch (HandException e) {
					resetOutput();
					sendToAll(e);
					e.printStackTrace();
					return;
				}
				resetOutput();
				sendToAll(HubGamePlay);
				break;
			}
			
		}

	}

}
