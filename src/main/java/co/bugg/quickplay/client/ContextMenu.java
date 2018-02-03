package co.bugg.quickplay.client;

import java.util.ArrayList;
import java.util.List;

public interface ContextMenu {

    List<String> options = new ArrayList<>();

   void optionSelected(int index);
}
