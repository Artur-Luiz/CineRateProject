package app.cinerate.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class PaginatorComponent extends HorizontalLayout {

    private int currentPage = 1;

    private transient Consumer<Integer> pageChangeListener;

    public PaginatorComponent() {
        createPaginator();
    }

    private void createPaginator() {
        removeAll();

        HorizontalLayout paginatorLayout = new HorizontalLayout();
        paginatorLayout.setSpacing(true);

        var prevButton = new Button("Anterior");
        if (currentPage == 1) {
            prevButton.setEnabled(false);
        } else {
            prevButton.addClickListener(event -> {
                currentPage--;
                if (pageChangeListener != null)
                    pageChangeListener.accept(currentPage);
                createPaginator();
            });
        }

        paginatorLayout.add(prevButton);

        List<Button> buttons = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> new Button(String.valueOf(i)))
                .toList();

        buttons.forEach(this::add);

        // logic: prev 1 2 3 4 5 next
        //             ^
        // logic: prev 1 2 3 4 5 next
        //               ^
        // logic: prev 1 2 3 4 5 next
        //                 ^
        // logic: prev 2 3 4 5 6 next
        //                 ^
        // logic: prev 3 4 5 6 7 next
        //                 ^

        int index = 0;
        for (Button button : buttons) {
            if (currentPage <= 3) {
                button.setText(String.valueOf(index + 1));
            } else {
                button.setText(String.valueOf(currentPage - 2 + index));
            }


            button.addClickListener(event -> {
                if (!canGoNext())
                    return;

                currentPage = Integer.parseInt(button.getText());
                if (pageChangeListener != null)
                    pageChangeListener.accept(currentPage);
                createPaginator();
            });
            paginatorLayout.add(button);

            if (button.getText().equals(String.valueOf(currentPage))) {
                button.setEnabled(false);
            }

            index++;
        }


        var nextButton = new Button("Próximo");
        nextButton.addClickListener(event -> {
            if (!canGoNext())
                return;

            currentPage++;
            if (pageChangeListener != null)
                pageChangeListener.accept(currentPage);
            createPaginator();
        });

        paginatorLayout.add(nextButton);

        add(paginatorLayout);
    }

    // sempre tem alguem que vai tentar...
    private boolean canGoNext() {
        return currentPage < Integer.MAX_VALUE - 10;
    }


    public int getCurrentPage() {
        return currentPage;
    }

    public void onPageChangeListener(Consumer<Integer> pageChangeListener) {
        this.pageChangeListener = pageChangeListener;
    }

}