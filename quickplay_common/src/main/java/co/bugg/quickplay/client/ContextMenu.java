package co.bugg.quickplay.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic context menu interface without any rendering or input handling
 */
public interface ContextMenu {

    /**
     * The list of options in the context menu
     */
    List<String> options = new ArrayList<>();

    /**
     * Called whenever an option is selected
     * @param index Index of the option selected
     */
    void optionSelected(int index);
}
