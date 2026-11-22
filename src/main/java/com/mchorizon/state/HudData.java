package com.mchorizon.state;

import java.util.ArrayList;
import java.util.List;

/** Mirrors the JSON MCEvents sends (see PROTOCOL.md). Missing fields keep their defaults. */
public class HudData {
	public int v;
	public EventInfo event = new EventInfo();
	public GameInfo game = new GameInfo();
	public List<Team> teams = new ArrayList<>();
	public List<LiveRow> live = new ArrayList<>();
	public You you = new You();
	public MyTeam team = new MyTeam();

	public static class EventInfo {
		public String name = "";
		public String title = "";
		public boolean active;
		public boolean paused;
		public boolean mini;
		public String phase = "";
		public String phaseId = "";
		public int round;
		public int rounds;
		public String multiplier = "";
		public int timeLeft;
	}

	public static class GameInfo {
		public boolean active;
		public String name = "";
		public String color = "#FFAA00";
		public String map = "";
		public int round;
		public int rounds;
		public int timeLeft;
	}

	public static class Team {
		public String id = "";
		public String name = "";
		public String color = "#FFFFFF";
		public boolean hidden;
		public int rank;
		public int points;
		public int gamePoints;
		public int index;
		public List<Member> members = new ArrayList<>();
	}

	public static class Member {
		public String uuid = "";
		public String name = "";
		public boolean online;
		public int points;
	}

	public static class LiveRow {
		public String id = "";
		public int points;
	}

	public static class You {
		public String name = "";
		public boolean spectator;
		public int points;
		public int place;
		public int of;
		public Integer gamePoints;
		public Integer gamePlace;
		public Integer gameOf;
		public String tier = "";
		public Integer events;
		public Long lifetime;
		public Integer bestPlace;
		public Integer overallRank;
		public Integer overallOf;
	}

	public static class MyTeam extends Team {
		public int of;
		public String behindTeam;
		public Integer behindBy;
		public String leadTeam;
		public Integer leadBy;
		public Integer gamePlace;
		public Integer gameOf;
		public List<String> tiedWith = new ArrayList<>();
	}

	public Team teamById(String id) {
		if (id == null) {
			return null;
		}
		for (Team t : teams) {
			if (t.id.equals(id)) {
				return t;
			}
		}
		return null;
	}
}
