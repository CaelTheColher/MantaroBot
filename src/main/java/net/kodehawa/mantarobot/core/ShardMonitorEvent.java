package net.kodehawa.mantarobot.core;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.events.Event;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class ShardMonitorEvent extends Event {
	private class ShardListeners {
		private final int shardId;
		private boolean commandListener = false;
		private boolean mantaroListener = false;
		private boolean voiceChannelListener = false;

		private ShardListeners(int shardId) {
			this.shardId = shardId;
		}

		@Override
		public int hashCode() {
			return shardId;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof ShardListeners && ((ShardListeners) other).shardId == shardId;
		}

		private void alive(int listener) {
			switch (listener) {
				case MANTARO_LISTENER:
					mantaroListener = true;
					break;
				case COMMAND_LISTENER:
					commandListener = true;
					break;
				case VOICE_CHANNEL_LISTENER:
					voiceChannelListener = true;
					break;
				default:
					throw new IllegalArgumentException("Unknown listener id " + listener);
			}
			if (mantaroListener && commandListener && voiceChannelListener) {
				log.debug("Shard " + shardId + " is alive");
				dead.remove(this);
				alive.add(this);
			}
		}
	}

	public static final int
		MANTARO_LISTENER = 0,
		COMMAND_LISTENER = 1,
		VOICE_CHANNEL_LISTENER = 2;
	private Set<ShardListeners> alive = new CopyOnWriteArraySet<>();
	private Set<ShardListeners> dead = new CopyOnWriteArraySet<>();

	public ShardMonitorEvent(int shards) {
		super(null, 0);
		for (int i = 0; i < shards; i++)
			dead.add(new ShardListeners(i));
	}

	public void alive(int shard, int listener) {
		dead.stream().filter(s -> s.shardId == shard).forEach(s -> s.alive(listener));
	}

	public int[] getAliveShards() {
		return alive.stream().mapToInt(s -> s.shardId).toArray();
	}

	public int[] getDeadShards() {
		return dead.stream().mapToInt(s -> s.shardId).toArray();
	}

	public boolean isAlive(int shard) {
		return alive.stream().map(s -> s.shardId == shard).count() != 0;
	}

	public int totalAliveShards() {
		return alive.size();
	}
}
