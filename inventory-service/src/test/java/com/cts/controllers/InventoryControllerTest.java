package com.cts.controllers;

import com.cts.dtos.InventoryDto;
import com.cts.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerTest {

    // MockMvc is the main tool for simulating HTTP requests to our controller
    @Autowired
    private MockMvc mockMvc;

    // Created a mock of the service layer because we only want to test the controller
    @MockitoBean
    private InventoryService inventoryService;


    @Autowired
    private ObjectMapper objectMapper;      // ObjectMapper helps convert our Java objects to JSON strings for the request body

    private InventoryDto inventoryDto;

    @BeforeEach
    void init() {
        // This runs before each test to set up our test data
        inventoryDto = new InventoryDto(1L, 101L, 50, "A1-North", LocalDateTime.now());
    }

    @Test
    void addInventory() throws Exception {      //It Should Return 201_Created

        when(inventoryService.addInventory(any(InventoryDto.class))).thenReturn(inventoryDto);

        // Act & Assert
        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").value(101L));
    }

    @Test
    void addInventory_WithInvalidData() throws Exception {      //It Should Return 400_BadRequest
        // Arrange: DTO with a null itemId to trigger validation
        InventoryDto invalidDto = new InventoryDto(null, null, 50, "A1-North", null);

        // Act & Assert
        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllInventory() throws Exception {       //It Should Return 200_OK And List

        when(inventoryService.getAllInventory()).thenReturn(List.of(inventoryDto));

        // Act & Assert
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].warehouseLocation").value("A1-North"));
    }

    @Test
    void getInventoryById() throws Exception {      //It Should Return 200_OK

        when(inventoryService.getInventoryById(1L)).thenReturn(inventoryDto);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateInventory() throws Exception {       //It Should Return 200_OK

        when(inventoryService.updateInventory(eq(1L), any(InventoryDto.class))).thenReturn(inventoryDto);

        // Act & Assert
        mockMvc.perform(put("/api/inventory/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(50));
    }

    @Test
    void deleteInventory() throws Exception {       //It Should Return 200_OK

        String successMessage = "Inventory with ID 1 deleted successfully.";
        when(inventoryService.deleteInventory(1L)).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(delete("/api/inventory/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));
    }
}
