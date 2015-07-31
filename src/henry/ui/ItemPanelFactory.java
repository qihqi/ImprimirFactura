package henry.ui;

import henry.model.Item;
import henry.api.FacturaInterface;

public class ItemPanelFactory {
    private FacturaInterface api;
    private SearchDialog dialog;

    public ItemPanelFactory(FacturaInterface api, SearchDialog dialog) {
        this.api = api;
        this.dialog = dialog;
    }

    public ItemPanel make(ItemContainer container, Item item) {
        return new ItemPanel(container, item, api, dialog);
    }
}
