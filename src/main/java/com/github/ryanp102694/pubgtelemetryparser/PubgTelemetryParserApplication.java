package com.github.ryanp102694.pubgtelemetryparser;

import com.github.ryanp102694.pubgtelemetryparser.data.GameData;
import com.github.ryanp102694.pubgtelemetryparser.event.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;


/*
 *
 * I'm going to want to build some "global" objects that I can reference.
 * (teamMap for looking up team info, gameState for looking up circle, packages, zones, etc)
 *
 * I want to basically iterate through all of the events and have handlers events. Not every event will have a handler. The handler
 * will be passed the teamMap, gameState, and potentially other info. The handler should have a list of keys for data points
 * that it calculates and can attach to playerState objects. The handler logic may update multiple playerState objects as it sees fit.
 *
 * The handler will then create new playerState objects. The player state objects should be able to hold arbitrary
 * data about the player, which will ultimately be used to build my csv training data.
 *
 * I want to be able to grab the state at any arbitrary time for any player. So I wil
 *
 *
 * Map<String, List<PlayerState>>    <---- this object will hold the data for players, the PlayerState can be updated as events are iterated
 *
 */
@SpringBootApplication
public class PubgTelemetryParserApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(PubgTelemetryParserApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		GameData gameData = new GameData();

		TelemetryEventHandler matchDefinitionHandler = new MatchDefinitionEventHandler();
		TelemetryEventHandler matchStartEventHandler = new MatchStartEventHandler();
		TelemetryEventHandler playerPositionEventHandler = new PlayerPositionEventHandler();
		TelemetryEventHandler parachuteLandingEventHandler = new ParachuteLandingEventHandler();
		TelemetryEventHandler gameStatePeriodicEventHandler = new GameStatePeriodicEventHandler();


		String jsonString = new String(Files.readAllBytes(Paths.get("telemetry.json")));
		JSONArray telemetryEvents = new JSONArray(jsonString);
		Set<String> eventTypes = new TreeSet<>();

		for(int i = 0; i < telemetryEvents.length(); i++){
			JSONObject telemetryEvent = telemetryEvents.getJSONObject(i);

			String eventType = telemetryEvent.getString("_T");
			eventTypes.add(eventType);

			Map<Integer, Set<String>> teams = null;


			//can probably throw lots of things out until the match starts
			if("LogMatchDefinition".equals(eventType)){
				matchDefinitionHandler.handle(telemetryEvent, gameData);
			}else if("LogMatchStart".equals(eventType)){
				matchStartEventHandler.handle(telemetryEvent, gameData);
			}else if("LogParachuteLanding".equals(eventType)){
				parachuteLandingEventHandler.handle(telemetryEvent, gameData);
			}else if("LogPlayerPosition".equals(eventType)){
				playerPositionEventHandler.handle(telemetryEvent, gameData);
			}else if("LogGameStatePeriodic".equals(eventType)){
				gameStatePeriodicEventHandler.handle(telemetryEvent, gameData);
			}

		}

		Instant gameStart = gameData.getStartTime();

		GameDataWriter gameDataWriter = new GameDataWriter();
		gameDataWriter.writeGameDataPoints(gameData);

	}
}
