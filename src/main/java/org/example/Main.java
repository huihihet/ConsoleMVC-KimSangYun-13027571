package org.example;

import org.example.app.Router;
import org.example.controller.SampleController;
import org.example.model.repository.InMemorySampleRepository;
import org.example.model.repository.SampleRepository;
import org.example.view.SampleView;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner          scanner    = new Scanner(System.in);
        SampleRepository repository = new InMemorySampleRepository();
        SampleView       view       = new SampleView();
        SampleController controller = new SampleController(repository, view, scanner);
        Router           router     = new Router(controller);

        while (true) {
            view.printMenu();
            try {
                int menu = Integer.parseInt(scanner.nextLine().trim());
                if (!router.route(menu)) break;
            } catch (NumberFormatException e) {
                view.printError("메뉴는 숫자로 입력하세요.");
            } catch (java.util.NoSuchElementException e) {
                break;
            }
        }
    }
}
