package org.example.controller;

import org.example.model.entity.Sample;
import org.example.model.repository.SampleRepository;
import org.example.view.SampleView;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class SampleController {

    private final SampleRepository repository;
    private final SampleView       view;
    private final Scanner          scanner;

    public SampleController(SampleRepository repository, SampleView view, Scanner scanner) {
        this.repository = repository;
        this.view       = view;
        this.scanner    = scanner;
    }

    public void register() {
        view.printPrompt("이름: ");
        String name = scanner.nextLine();

        view.printPrompt("평균 생산 시간(min): ");
        String timeStr = scanner.nextLine();
        int avgProductionTime;
        try {
            avgProductionTime = Integer.parseInt(timeStr);
        } catch (NumberFormatException e) {
            view.printError("평균 생산 시간은 숫자로 입력하세요.");
            return;
        }

        view.printPrompt("수율 (0.0 초과 ~ 1.0 이하): ");
        String yieldStr = scanner.nextLine();
        double yield;
        try {
            yield = Double.parseDouble(yieldStr);
        } catch (NumberFormatException e) {
            view.printError("수율은 숫자로 입력하세요.");
            return;
        }

        Sample sample;
        try {
            sample = new Sample(name, avgProductionTime, yield, 0);
        } catch (IllegalArgumentException e) {
            view.printError(e.getMessage());
            return;
        }

        repository.save(sample);
        view.printSuccess("시료가 등록되었습니다.");
    }

    public void listAll() {
        List<Sample> list = repository.findAll();
        if (list.isEmpty()) {
            view.printEmpty();
        } else {
            view.printSampleList(list);
        }
    }

    public void findById() {
        view.printPrompt("조회할 시료 ID: ");
        String idStr = scanner.nextLine();
        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            view.printError("ID는 숫자로 입력하세요.");
            return;
        }

        Optional<Sample> result = repository.findById(id);
        if (result.isEmpty()) {
            view.printError("해당 시료를 찾을 수 없습니다.");
            return;
        }
        view.printSampleDetail(result.get());
    }

    public void update() {
        view.printPrompt("수정할 시료 ID: ");
        String idStr = scanner.nextLine();
        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            view.printError("ID는 숫자로 입력하세요.");
            return;
        }

        Optional<Sample> found = repository.findById(id);
        if (found.isEmpty()) {
            view.printError("해당 시료를 찾을 수 없습니다.");
            return;
        }
        Sample sample = found.get();

        view.printUpdateFieldMenu();
        String choice = scanner.nextLine();
        switch (choice) {
            case "1" -> {
                view.printPrompt("이름: ");
                String newName = scanner.nextLine();
                try {
                    sample.updateName(newName);
                } catch (IllegalArgumentException e) {
                    view.printError(e.getMessage());
                    return;
                }
            }
            case "2" -> {
                view.printPrompt("평균 생산 시간(min): ");
                String timeStr = scanner.nextLine();
                try {
                    sample.updateAvgProductionTime(Integer.parseInt(timeStr));
                } catch (NumberFormatException e) {
                    view.printError("평균 생산 시간은 숫자로 입력하세요.");
                    return;
                } catch (IllegalArgumentException e) {
                    view.printError(e.getMessage());
                    return;
                }
            }
            case "3" -> {
                view.printPrompt("수율 (0.0 초과 ~ 1.0 이하): ");
                String yieldStr = scanner.nextLine();
                try {
                    sample.updateYield(Double.parseDouble(yieldStr));
                } catch (NumberFormatException e) {
                    view.printError("수율은 숫자로 입력하세요.");
                    return;
                } catch (IllegalArgumentException e) {
                    view.printError(e.getMessage());
                    return;
                }
            }
            case "4" -> {
                view.printPrompt("재고: ");
                String stockStr = scanner.nextLine();
                try {
                    sample.updateStock(Integer.parseInt(stockStr));
                } catch (NumberFormatException e) {
                    view.printError("재고는 숫자로 입력하세요.");
                    return;
                } catch (IllegalArgumentException e) {
                    view.printError(e.getMessage());
                    return;
                }
            }
            default -> {
                view.printError("유효하지 않은 선택입니다.");
                return;
            }
        }

        repository.update(sample);
        view.printSuccess("시료가 수정되었습니다.");
    }

    public void delete() {
        view.printPrompt("삭제할 시료 ID: ");
        String idStr = scanner.nextLine();
        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            view.printError("ID는 숫자로 입력하세요.");
            return;
        }

        if (repository.deleteById(id)) {
            view.printSuccess("시료가 삭제되었습니다.");
        } else {
            view.printError("해당 시료를 찾을 수 없습니다.");
        }
    }

    public void searchByName() {
        view.printPrompt("검색할 이름: ");
        String keyword = scanner.nextLine();
        List<Sample> list = repository.findByNameContaining(keyword);
        if (list.isEmpty()) {
            view.printEmpty();
        } else {
            view.printSampleList(list);
        }
    }

    public void handleInvalidMenu() {
        view.printError("유효하지 않은 메뉴입니다.");
    }
}
