package dst.courseproject.services.impl;

import dst.courseproject.entities.Category;
import dst.courseproject.entities.Video;
import dst.courseproject.exceptions.CategoryAlreadyExistsException;
import dst.courseproject.models.binding.CategoryAddBindingModel;
import dst.courseproject.models.binding.CategoryEditBindingModel;
import dst.courseproject.models.service.CategoryServiceModel;
import dst.courseproject.models.view.CategoryViewModel;
import dst.courseproject.repositories.CategoryRepository;
import dst.courseproject.services.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    private void checkIfCategoryExist(String name) throws CategoryAlreadyExistsException {
        Category category = this.categoryRepository.findByName(name);
        if (category != null) {
            throw new CategoryAlreadyExistsException("Category already exists!");
        }
    }

    private void mapCategoriesToViewModels(List<Category> categories, Set<CategoryViewModel> viewModels) {
        for (Category category : categories) {
            viewModels.add(this.modelMapper.map(category, CategoryViewModel.class));
        }
    }

    private void removeCategoryVideos(Category category) {
        for (Video video : category.getVideos()) {
            video.setDeletedOn(LocalDate.now());
        }
    }

    private void extractNames(List<Category> categories, Set<String> names) {
        for (Category category : categories) {
            names.add(category.getName());
        }
    }

    @Override
    public CategoryServiceModel addCategory(@Valid CategoryAddBindingModel categoryBindingModel) throws CategoryAlreadyExistsException {
        this.checkIfCategoryExist(categoryBindingModel.getName());
        Category category = this.modelMapper.map(categoryBindingModel, Category.class);
        this.categoryRepository.save(category);

        return this.modelMapper.map(category, CategoryServiceModel.class);
    }

    @Override
    public Set<CategoryViewModel> getAllCategoriesAsViewModels() {
        List<Category> categories = this.categoryRepository.findAllByDeletedOnNull();
        Set<CategoryViewModel> viewModels = new LinkedHashSet<>();
        this.mapCategoriesToViewModels(categories, viewModels);

        return viewModels;
    }

    @Override
    public CategoryServiceModel getCategoryServiceModel(String id) {
        Category category = this.categoryRepository.findByIdAndDeletedOnNull(id);
        CategoryServiceModel categoryServiceModel = this.modelMapper.map(category, CategoryServiceModel.class);

        return categoryServiceModel;
    }

    @Override
    public CategoryServiceModel editCategory(@Valid CategoryEditBindingModel categoryEditBindingModel, String id) {
        Category category = this.categoryRepository.findByIdAndDeletedOnNull(id);
        category.setName(categoryEditBindingModel.getName());

        this.categoryRepository.save(category);
        return this.modelMapper.map(category, CategoryServiceModel.class);
    }

    @Override
    public CategoryServiceModel deleteCategory(String id) {
        Category category = this.categoryRepository.findByIdAndDeletedOnNull(id);
        category.setDeletedOn(LocalDate.now());
        this.removeCategoryVideos(category);

        return this.modelMapper.map(category, CategoryServiceModel.class);
    }

    @Override
    public CategoryViewModel getCategoryViewModel(String name) {
        Category category = this.categoryRepository.findByNameAndDeletedOnNull(name);
        CategoryViewModel viewModel = this.modelMapper.map(category, CategoryViewModel.class);

        return viewModel;
    }

    @Override
    public Set<String> getCategoriesNames() {
        List<Category> categories = this.categoryRepository.findAllByDeletedOnNull();
        Set<String> names = new HashSet<>();
        this.extractNames(categories, names);

        return names;
    }

    @Override
    public CategoryServiceModel getCategoryServiceModelByName(String name) {
        Category category = this.categoryRepository.findByNameAndDeletedOnNull(name);
        CategoryServiceModel categoryServiceModel = this.modelMapper.map(category, CategoryServiceModel.class);

        return categoryServiceModel;
    }
}
