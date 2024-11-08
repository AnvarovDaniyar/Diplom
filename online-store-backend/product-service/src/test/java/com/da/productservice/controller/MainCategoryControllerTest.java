package com.da.productservice.controller;

import static com.da.productservice.util.RandomEntityGenerator.createMainCategory;
import static com.da.productservice.util.RandomEntityGenerator.createProduct;
import static com.da.productservice.util.RandomEntityGenerator.createSubCategory;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.da.productservice.entity.MainCategory;
import com.da.productservice.entity.Product;
import com.da.productservice.entity.SubCategory;
import com.da.productservice.mapper.ProductMapperImpl;
import com.da.productservice.mapper.SubCategoryMapperImpl;
import com.da.productservice.repository.MainCategoryRepository;
import com.da.productservice.repository.ProductRepository;
import com.da.productservice.repository.SubCategoryRepository;
import com.da.productservice.service.MainCategoryServiceImpl;
import com.da.productservice.service.ProductServiceImpl;
import com.da.productservice.service.SubCategoryServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest
@Import({ MainCategoryServiceImpl.class, ProductServiceImpl.class, ProductMapperImpl.class, SubCategoryServiceImpl.class, SubCategoryMapperImpl.class })
class MainCategoryControllerTest {

  @MockBean
  private MainCategoryRepository mainCategoryRepository;

  @MockBean
  private SubCategoryRepository subCategoryRespository;

  @MockBean
  private ProductRepository productRepository;

  @Autowired
  private MockMvc mockMvc;

  private static final MediaType JSON = MediaType.APPLICATION_JSON;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final MainCategory MAINCATEGORY = createMainCategory();
  private static final SubCategory SUBCATEGORY = createSubCategory();
  private static final Product PRODUCT = createProduct();

  @BeforeEach
  public void setUp() {
    BDDMockito.when(mainCategoryRepository.save(any(MainCategory.class))).thenReturn(MAINCATEGORY);

    BDDMockito.when(mainCategoryRepository.updateName(anyString(), anyLong())).thenReturn(1);

    BDDMockito.when(subCategoryRespository.findById(anyLong())).thenReturn(Optional.of(SUBCATEGORY));

    BDDMockito.when(mainCategoryRepository.findById(anyLong())).thenReturn(Optional.of(MAINCATEGORY));

    BDDMockito.doNothing().when(subCategoryRespository).delete(any(SubCategory.class));

    BDDMockito.when(productRepository.findByMainCategoryMainCategoryId(anyLong())).thenReturn(List.of(PRODUCT));

    BDDMockito.when(mainCategoryRepository.findByMainCategoryName(anyString())).thenReturn(Optional.of(MAINCATEGORY));

    BDDMockito.when(mainCategoryRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(MAINCATEGORY)));
  }

  @Test
  public void createMainCategory_Return201StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(post("/main-categories").contentType(JSON)
                                            .content(OBJECT_MAPPER.writeValueAsString(MAINCATEGORY))
                                            .accept(JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void createMainCategory_Return400StatusCode_WhenMainCategoryHasInvalidFields() throws Exception{
    mockMvc.perform(post("/main-categories").contentType(JSON)
                                            .content(OBJECT_MAPPER.writeValueAsString(new MainCategory()))
                                            .accept(JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void updateMainCategory_Return204StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(put("/main-categories/1?mainCategoryName=maincategory"))
            .andExpect(status().isNoContent());
  }

  @Test
  public void updateMainCategory_Return404StatusCode_WhenUpdateOperationReturnsZero() throws Exception{
    BDDMockito.when(mainCategoryRepository.updateName(anyString(), anyLong())).thenReturn(0);

    mockMvc.perform(put("/main-categories/1?mainCategoryName=maincategory"))
            .andExpect(status().isNotFound());
  }

  @Test
  public void deleteMainCategory_Return204StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(delete("/main-categories/1")).andExpect(status().isNoContent());
  }

  @Test
  public void deleteMainCategory_Return404StatusCode_WhenMainCategoryWasNotFound() throws Exception{
    BDDMockito.when(mainCategoryRepository.findById(anyLong())).thenReturn(Optional.empty());

    mockMvc.perform(delete("/main-categories/1")).andExpect(status().isNotFound());
  }

  @Test
  public void getMainCategoryByName_Return200StatusCode_WhenSuccesful() throws Exception{
    mockMvc.perform(get("/main-categories/maincategory").accept(JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void getMainCategoryByName_Return404StatusCode_WhenMainCategoryWasNotFound() throws Exception{
    BDDMockito.when(mainCategoryRepository.findByMainCategoryName(anyString())).thenReturn(Optional.empty());

    mockMvc.perform(get("/main-categories/maincategory").accept(JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void getAllMainCategory_Return200StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(get("/main-categories?page=0&size=10").accept(JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void getAllMainCategory_Return404StatusCode_WhenNoMainCategoriesAreFound() throws Exception{
    BDDMockito.when(mainCategoryRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

    mockMvc.perform(get("/main-categories?page=0&size=10").accept(JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }
}
