package com.github.ryanp102694.pubgtelemetryparser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@SpringBootApplication
public class PubgTelemetryParserApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(PubgTelemetryParserApplication.class, args);
	}


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

	@Override
	public void run(String... args) throws Exception {

		String jsonString = new String(Files.readAllBytes(Paths.get("telemetry.json")));

		JSONArray telemetryEvents = new JSONArray(jsonString);


		Set<String> eventTypes = new TreeSet<>();


		for(int i = 0; i < telemetryEvents.length(); i++){
			JSONObject telemetryEvent = telemetryEvents.getJSONObject(i);

			String eventType = telemetryEvent.getString("_T");
			eventTypes.add(eventType);

			Map<Integer, Set<String>> teams = null;

			if("LogMatchStart".equals(eventType)){
				teams = buildTeams(telemetryEvent);

				for(Integer key : teams.keySet()){
					System.out.println(key + ": " + teams.get(key));
				}

			}

		}

		eventTypes.forEach(System.out::println);

	}

	public Map<Integer, Set<String>> buildTeams(JSONObject matchStartEvent){
		if(!"LogMatchStart".equals(matchStartEvent.getString("_T"))){
			throw new RuntimeException("Must build teams from LogMatchStart");
		}

		Map<Integer, Set<String>> teamMap = new HashMap<>();

		JSONArray charactersArray = matchStartEvent.getJSONArray("characters");

		for(int i = 0; i < charactersArray.length(); i++){
			JSONObject character = charactersArray.getJSONObject(i);
			if(!teamMap.containsKey(character.getInt("teamId"))){
				teamMap.put(character.getInt("teamId"), new HashSet<>());
			}
			teamMap.get(character.getInt("teamId")).add(character.getString("name"));

		}
		return teamMap;
	}

}
