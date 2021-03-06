package net.kodehawa.mantarobot.utils.crossbot;

import lombok.SneakyThrows;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class CrossBotAction<T> {
	public static final Consumer<Throwable> DEFAULT_FAILURE = Throwable::printStackTrace;
	public static final Consumer DEFAULT_SUCCESS = o -> {
	};

	public static <T> CrossBotAction<T> of(ExecutorService es, Supplier<T> supplier) {
		return new CrossBotAction<T>(es) {
			@Override
			public Future<T> submit() {
				return new CompletableFuture<T>() {
					@Override
					public T get() throws InterruptedException, ExecutionException {
						return supplier.get();
					}
				};
			}
		};
	}

	private final Executor executor;

	public CrossBotAction(Executor executor) {
		this.executor = executor;
	}

	public abstract Future<T> submit();

	@SneakyThrows
	public T complete() {
		try {
			return submit().get();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}

	@SuppressWarnings("unchecked")
	public void queue(Consumer<T> success, Consumer<Throwable> failure) {
		executor.execute(() -> {
			try {
				(success == null ? DEFAULT_SUCCESS : success).accept(complete());
			} catch (Throwable t) {
				(failure == null ? DEFAULT_FAILURE : failure).accept(t);
			}
		});
	}

	public void queue(Consumer<T> success) {
		queue(success, null);
	}

	public void queue() {
		queue(null, null);
	}
}
