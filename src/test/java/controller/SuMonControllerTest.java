package controller;

import dao.ActivityLogDAO;
import dao.DynastyDAO;
import dao.UserDAO;
import entity.Dynasty;
import entity.User;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.*;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Timeout(10)
class SuMonControllerTest {
    final HttpServletRequest request = mock(HttpServletRequest.class);
    final HttpServletResponse response = mock(HttpServletResponse.class);
    final HttpSession session = mock(HttpSession.class);
    final RequestDispatcher dispatcher = mock(RequestDispatcher.class);

    @BeforeEach void configureRequest() {
        when(request.getContextPath()).thenReturn("/SuMon");
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        param("username", "qa_su_mon");
        param("fullname", "QA SuMon");
        param("password", "Abc123");
    }

    void param(String key, String value) {
        when(request.getParameter(key)).thenReturn(value);
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 6, 7})
    void registrationPasswordBoundary(int length) throws Exception {
        String password = "a".repeat(length);
        param("password", password);
        try (var users = mockConstruction(UserDAO.class,
                (dao, context) -> when(dao.insert(any(User.class))).thenReturn(true))) {
            new RegisterServlet().doPost(request, response);
            var saved = ArgumentCaptor.forClass(User.class);
            verify(users.constructed().get(0)).insert(saved.capture());
            assertEquals("qa_su_mon", saved.getValue().getUsername());
            assertEquals("QA SuMon", saved.getValue().getFullname());
            assertNotEquals(password, saved.getValue().getPassword());
            assertTrue(BCrypt.checkpw(password, saved.getValue().getPassword()));
            verify(response).sendRedirect("/SuMon/login");
        }
    }

    @Test void duplicateRegistration() throws Exception {
        try (var users = mockConstruction(UserDAO.class, (dao, context) ->
                when(dao.findByUsername("qa_su_mon")).thenReturn(new User()))) {
            new RegisterServlet().doPost(request, response);
            verify(users.constructed().get(0), never()).insert(any());
            verify(request).setAttribute("error", "Tài khoản đã tồn tại");
            verify(dispatcher).forward(request, response);
        }
    }

    @Test void unsuccessfulInsert() throws Exception {
        try (var users = mockConstruction(UserDAO.class)) {
            new RegisterServlet().doPost(request, response);
            verify(users.constructed().get(0)).insert(any());
            verify(request).setAttribute("error", "Đăng ký thất bại");
            verify(dispatcher).forward(request, response);
            verify(response, never()).sendRedirect(anyString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "locked", "wrong", "valid"})
    void loginOutcomes(String scenario) throws Exception {
        User user = new User();
        user.setId(7);
        user.setActive(!scenario.equals("locked"));
        user.setPassword(BCrypt.hashpw("Abc123", BCrypt.gensalt(4)));
        if (scenario.equals("wrong")) param("password", "Wrong1");
        try (var users = mockConstruction(UserDAO.class, (dao, context) ->
                when(dao.findByUsername("qa_su_mon")).thenReturn(scenario.equals("unknown") ? null : user));
             var logs = mockConstruction(ActivityLogDAO.class)) {
            new LoginServlet().doPost(request, response);
            verify(users.constructed().get(0)).findByUsername("qa_su_mon");
            if (scenario.equals("valid")) {
                verify(session).setAttribute("user", user);
                verify(logs.constructed().get(0)).insert(7, "LOGIN", "Đăng nhập");
                verify(response).sendRedirect("/SuMon/");
            } else {
                verify(request).setAttribute("error", scenario.equals("locked")
                        ? "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên."
                        : "Sai tài khoản hoặc mật khẩu");
                verify(dispatcher).forward(request, response);
                verifyNoInteractions(session);
                assertTrue(logs.constructed().isEmpty());
            }
        }
    }

    void dynastyForm(String action, String start, String end) {
        param("action", action);
        param("name", "Triều đại thử");
        param("founder", "Người sáng lập");
        param("capital", "Kinh đô");
        param("description", "Mô tả");
        param("startYear", start);
        param("endYear", end);
    }

    @ParameterizedTest
    @CsvSource({"980,1009", "980,980", "1009,980"})
    void insertYearOrder(String start, String end) throws Exception {
        dynastyForm("insert", start, end);
        try (var dynasties = mockConstruction(DynastyDAO.class)) {
            new AdminDynastyController().doPost(request, response);
            var saved = ArgumentCaptor.forClass(Dynasty.class);
            verify(dynasties.constructed().get(0)).insert(saved.capture());
            assertEquals(Integer.parseInt(start), saved.getValue().getStartYear());
            assertEquals(Integer.parseInt(end), saved.getValue().getEndYear());
            assertEquals("Triều đại thử", saved.getValue().getName());
            verify(response).sendRedirect("/SuMon/admin/dynasties");
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc", "980.5", "2147483648"})
    void invalidStartYear(String start) {
        dynastyForm("insert", start, "1009");
        try (var dynasties = mockConstruction(DynastyDAO.class)) {
            var controller = new AdminDynastyController();
            assertThrows(NumberFormatException.class, () -> controller.doPost(request, response));
            verifyNoInteractions(dynasties.constructed().get(0));
        }
    }

    @Test void listAndLoadEdit() throws Exception {
        param("action", "edit");
        param("id", "7");
        Dynasty dynasty = new Dynasty();
        List<Dynasty> list = List.of(dynasty);
        try (var dynasties = mockConstruction(DynastyDAO.class, (dao, context) -> {
            when(dao.findById(7)).thenReturn(dynasty);
            when(dao.findAll()).thenReturn(list);
        })) {
            new AdminDynastyController().doGet(request, response);
            verify(dynasties.constructed().get(0)).findAll();
            verify(request).setAttribute("editDynasty", dynasty);
            verify(request).setAttribute("dynasties", list);
            verify(dispatcher).forward(request, response);
        }
    }

    @Test void updatePreservesEmptyFields() throws Exception {
        dynastyForm("update", "980", "1009");
        param("id", "7");
        param("name", "");
        param("founder", "");
        param("capital", "");
        param("description", "");
        Dynasty old = new Dynasty();
        old.setName("Cũ"); old.setFounder("Sáng lập cũ");
        old.setCapital("Kinh đô cũ"); old.setDescription("Mô tả cũ"); old.setImage("old.jpg");
        try (var dynasties = mockConstruction(DynastyDAO.class,
                (dao, context) -> when(dao.findById(7)).thenReturn(old))) {
            new AdminDynastyController().doPost(request, response);
            var saved = ArgumentCaptor.forClass(Dynasty.class);
            verify(dynasties.constructed().get(0)).update(saved.capture());
            Dynasty updated = saved.getValue();
            assertEquals(7, updated.getId());
            assertEquals("Cũ", updated.getName());
            assertEquals("Sáng lập cũ", updated.getFounder());
            assertEquals("Kinh đô cũ", updated.getCapital());
            assertEquals("Mô tả cũ", updated.getDescription());
            assertEquals("old.jpg", updated.getImage());
            verify(response).sendRedirect("/SuMon/admin/dynasties");
        }
    }

    @Test void deleteById() throws Exception {
        param("action", "delete");
        param("id", "7");
        try (var dynasties = mockConstruction(DynastyDAO.class)) {
            new AdminDynastyController().doGet(request, response);
            verify(dynasties.constructed().get(0)).delete(7);
            verify(response).sendRedirect("/SuMon/admin/dynasties");
        }
    }
}
