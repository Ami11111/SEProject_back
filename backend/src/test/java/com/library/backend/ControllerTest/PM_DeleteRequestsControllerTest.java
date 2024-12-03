package com.library.backend.ControllerTest;

import com.library.backend.controller.PM_DeleteRequestsController;
import org.junit.jupiter.api.Test;

import com.library.backend.dto.DeleteRequestsDTO;
import com.library.backend.entity.PM_Admin;
import com.library.backend.entity.PM_DeleteRequests;
import com.library.backend.entity.PM_Paper;
import com.library.backend.entity.PM_User;
import com.library.backend.repository.*;
import com.library.backend.service.PaperService;
import com.library.backend.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PM_DeleteRequestsControllerTest {

    @InjectMocks
    private PM_DeleteRequestsController controller;

    @Mock
    private PM_DeleteRequestsRepository deleteRequestsRepository;

    @Mock
    private PM_AdminRepository adminRepository;

    @Mock
    private PM_UserRepository userRepository;

    @Mock
    private PM_PaperRepository paperRepository;

    @Mock
    private PaperService paperService;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDeleteRequest_ValidAdmin() {
        // Setup
        String token = "Bearer valid.token";
        String adminId = "1";
        PM_Admin mockAdmin = new PM_Admin();
        mockAdmin.setId(1);
        PM_DeleteRequests mockRequest = new PM_DeleteRequests();
        mockRequest.setUserId(2);
        mockRequest.setDoi("10.1000/xyz123");

        List<PM_DeleteRequests> deleteRequests = Collections.singletonList(mockRequest);
        when(jwtUtil.extractUsername("valid.token")).thenReturn(adminId);
        when(adminRepository.findById(1)).thenReturn(mockAdmin);
        when(deleteRequestsRepository.findAll()).thenReturn(deleteRequests);

        // Act
        ResponseEntity<Object> response = controller.getDeleteRequest(token);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Success", body.get("message"));
        List<DeleteRequestsDTO> deletes = (List<DeleteRequestsDTO>) body.get("deletes");
        assertEquals(1, deletes.size());
        assertEquals("10.1000/xyz123", deletes.get(0).getDoi());

        verify(jwtUtil, times(1)).extractUsername("valid.token");
        verify(adminRepository, times(1)).findById(1);
        verify(deleteRequestsRepository, times(1)).findAll();
    }

    @Test
    void testGetDeleteRequest_InvalidAdmin() {
        // Setup
        String token = "Bearer invalid.token";
        when(jwtUtil.extractUsername("invalid.token")).thenReturn("2");
        when(adminRepository.findById(2)).thenReturn(null);

        // Act
        ResponseEntity<Object> response = controller.getDeleteRequest(token);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Access denied", body.get("message"));

        verify(jwtUtil, times(1)).extractUsername("invalid.token");
        verify(adminRepository, times(1)).findById(2);
    }

    @Test
    void testRemoveDeleteRequest_Success() {
        // Setup
        String token = "Bearer valid.token";
        String adminId = "1";
        String encodedDoi = Base64.getEncoder().encodeToString("10.1000/xyz123".getBytes());
        PM_Admin mockAdmin = new PM_Admin();
        mockAdmin.setId(1);

        when(jwtUtil.extractUsername("valid.token")).thenReturn(adminId);
        when(adminRepository.findById(1)).thenReturn(mockAdmin);
        when(deleteRequestsRepository.deleteByDoiAndUserId("10.1000/xyz123", 2)).thenReturn(1);

        // Act
        ResponseEntity<Object> response = controller.removeDeleteRequest(token, encodedDoi, 2);

        // Assert
        assertEquals(204, response.getStatusCodeValue());
        verify(jwtUtil, times(1)).extractUsername("valid.token");
        verify(adminRepository, times(1)).findById(1);
        verify(deleteRequestsRepository, times(1)).deleteByDoiAndUserId("10.1000/xyz123", 2);
    }

    @Test
    void testRequestDeletePaper_Success() {
        // Setup
        String token = "Bearer valid.token";
        String userId = "2";
        String encodedDoi = Base64.getEncoder().encodeToString("10.1000/xyz123".getBytes());
        PM_User mockUser = new PM_User();
        mockUser.setId(2);
        mockUser.setName("John Doe");
        PM_Paper mockPaper = new PM_Paper();
        mockPaper.setDoi("10.1000/xyz123");
        mockPaper.setFirstAuthor("John Doe");

        when(jwtUtil.extractUsername("valid.token")).thenReturn(userId);
        when(userRepository.findById(2)).thenReturn(mockUser);
        when(paperRepository.findByDoi("10.1000/xyz123")).thenReturn(mockPaper);
        when(paperService.isAuthorOfPaper("John Doe", "John Doe", null, null)).thenReturn(true);
        when(deleteRequestsRepository.existsByDoiAndUserId("10.1000/xyz123", 2)).thenReturn(false);

        // Act
        ResponseEntity<Object> response = controller.requestDeletePaper(token, encodedDoi);

        // Assert
        assertEquals(201, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Success", body.get("message"));

        verify(jwtUtil, times(1)).extractUsername("valid.token");
        verify(userRepository, times(1)).findById(2);
        verify(paperRepository, times(1)).findByDoi("10.1000/xyz123");
        verify(deleteRequestsRepository, times(1)).save(any(PM_DeleteRequests.class));
    }
}
