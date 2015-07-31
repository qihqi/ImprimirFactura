package henry.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseModel {
    private List<WeakReference<Listener>> listeners;

    public BaseModel() {
        listeners = new ArrayList<>();
    }

    public void notifyListeners() {
        for (WeakReference<Listener> wr : listeners) {
            Listener listener = wr.get();
            if (listener != null) {
                listener.onDataChanged();
            }
        }
    }

    public void addListener(Listener listener) {
        listeners.add(new WeakReference<>(listener));
    }

    public interface Listener {
        void onDataChanged();
    }
}
