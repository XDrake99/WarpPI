package org.warp.picalculator.flow;

import java.util.function.Function;

public class ObservableMap<T, U> extends Observable<U> {
	private Observable<T> originalObservable;
	private Function<T, U> mapAction;
	private volatile boolean initialized = false;
	private Disposable mapDisposable;

	public ObservableMap(Observable<T> originalObservable, Function<T, U> mapAction) {
		super();
		this.originalObservable = originalObservable;
		this.mapAction = mapAction;
	}

	private void initialize() {
		this.mapDisposable = originalObservable.subscribe((t) -> {
			for (Subscriber<? super U> sub : this.subscribers) {
				sub.onNext(mapAction.apply(t));
			} ;
		}, (e) -> {
			for (Subscriber<? super U> sub : this.subscribers) {
				sub.onError(e);
			} ;
		}, () -> {
			for (Subscriber<? super U> sub : this.subscribers) {
				sub.onComplete();
			} ;
		});
	}

	private void chechInitialized() {
		if (!initialized) {
			initialized = true;
			initialize();
		}
	}

	@Override
	public Disposable subscribe(Subscriber<? super U> sub) {
		Disposable disp = super.subscribe(sub);
		chechInitialized();
		return disp;
	}

	@Override
	public void onDisposed(Subscriber<? super U> sub) {
		super.onDisposed(sub);
		mapDisposable.dispose();
	}
}