package henry.model;

import lombok.Getter;
import lombok.Setter;
public class Observable<T> extends BaseModel {
    @Getter @Setter T ref;
}
