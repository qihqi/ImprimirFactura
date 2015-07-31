package henry.api;

import java.util.List;

public interface SearchEngine<T> {
    List<T> search(String prefijo);
}
