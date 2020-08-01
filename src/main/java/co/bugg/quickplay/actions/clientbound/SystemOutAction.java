package co.bugg.quickplay.actions.clientbound;

import co.bugg.quickplay.actions.Action;

public class SystemOutAction extends Action {
    @Override
    public void run() {
        System.out.println();
    }
}
