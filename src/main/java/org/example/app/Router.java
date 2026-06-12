package org.example.app;

import org.example.controller.SampleController;

public class Router {

    private final SampleController controller;

    public Router(SampleController controller) {
        this.controller = controller;
    }

    public boolean route(int menu) {
        switch (menu) {
            case 1 -> controller.register();
            case 2 -> controller.listAll();
            case 3 -> controller.findById();
            case 4 -> controller.update();
            case 5 -> controller.delete();
            case 6 -> controller.searchByName();
            case 0 -> { return false; }
            default -> controller.handleInvalidMenu();
        }
        return true;
    }
}
