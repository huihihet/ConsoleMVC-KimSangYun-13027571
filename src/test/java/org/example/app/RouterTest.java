package org.example.app;

import org.example.controller.SampleController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouterTest {

    @Mock SampleController controller;
    Router router;

    @BeforeEach
    void setUp() { router = new Router(controller); }

    @Test void route_1_register_호출_true_반환() { assertTrue(router.route(1)); verify(controller).register(); }
    @Test void route_2_listAll_호출_true_반환()  { assertTrue(router.route(2)); verify(controller).listAll(); }
    @Test void route_3_findById_호출_true_반환() { assertTrue(router.route(3)); verify(controller).findById(); }
    @Test void route_4_update_호출_true_반환()   { assertTrue(router.route(4)); verify(controller).update(); }
    @Test void route_5_delete_호출_true_반환()   { assertTrue(router.route(5)); verify(controller).delete(); }
    @Test void route_6_searchByName_호출_true_반환() { assertTrue(router.route(6)); verify(controller).searchByName(); }
    @Test void route_0_false_반환_메서드_미호출() {
        assertFalse(router.route(0));
        verifyNoInteractions(controller);
    }
    @Test void route_9_handleInvalidMenu_호출_true_반환() {
        assertTrue(router.route(9));
        verify(controller).handleInvalidMenu();
    }
}
