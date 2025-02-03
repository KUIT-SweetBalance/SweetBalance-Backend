package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.*;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageSize;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.beverage.BeverageCategory;
import com.sweetbalance.backend.repository.BeverageRepository;
import com.sweetbalance.backend.repository.BeverageSizeRepository;
import com.sweetbalance.backend.util.syrup.Syrup;
import com.sweetbalance.backend.util.syrup.SyrupManager;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BeverageServiceImpl implements BeverageService {

    private final BeverageRepository beverageRepository;
    private final BeverageSizeRepository beverageSizeRepository;

    @Autowired
    public BeverageServiceImpl(
            BeverageRepository beverageRepository,
            BeverageSizeRepository beverageSizeRepository) {
        this.beverageRepository = beverageRepository;
        this.beverageSizeRepository = beverageSizeRepository;
    }

    public List<String> getUniqueBrands() {
        return beverageRepository.findDistinctBrands();
    }

    public List<BrandPopularBeverageDTO> getPopularBeveragesByBrand(String brandName, int limit) {
        List<Beverage> beverages = beverageRepository.findTopBeveragesByBrandOrderByConsumeCountDesc(brandName, limit);
        return beverages.stream()
                .map(this::convertToBrandPopularBeverageDTO)
                .collect(Collectors.toList());
    }

    private BrandPopularBeverageDTO convertToBrandPopularBeverageDTO(Beverage beverage) {
        return BrandPopularBeverageDTO.builder()
                .beverageId(beverage.getBeverageId())
                .name(beverage.getName())
                .brand(beverage.getBrand())
                .imgUrl(beverage.getImgUrl())
                .consumeCount(beverage.getConsumeCount())
                .build();
    }

    @Override
    public BeverageDetailsDTO getBeverageDetails(Long beverageId) {

        Beverage beverage = beverageRepository.findById(beverageId)
                .orElseThrow(() -> new RuntimeException("Beverage not found"));

        List<Syrup> syrups = SyrupManager.getSyrupListOfBrand(beverage.getBrand());
        List<String> syrupNames = syrups.stream()
                .map(Syrup::getSyrupName)
                .collect(Collectors.toList());

        List<BeverageSizeDetailsWithRecommendDTO> sizeDetails = beverage.getSizes().stream()
                .map(this::createBeverageSizeDetailsWithRecommend)
                .collect(Collectors.toList());

        return BeverageDetailsDTO.builder()
                .beverageId(beverage.getBeverageId())
                .name(beverage.getName())
                .brand(beverage.getBrand())
                .imgUrl(beverage.getImgUrl())
                .syrups(syrupNames)
                .sizeDetails(sizeDetails)
                .build();
    }

    private BeverageSizeDetailsWithRecommendDTO createBeverageSizeDetailsWithRecommend(BeverageSize size) {
        List<BeverageSize> similarSizes = beverageSizeRepository.findTopSimilarSizesByBrandAndSugar(
                size.getBeverage().getBeverageId(),
                size.getBeverage().getBrand(),
                size.getSugar(),
                5);

        List<RecommendedBeverageDTO> recommends = similarSizes.stream()
                .map(similarSize -> convertToRecommendedBeverageDTO(similarSize, size.getSugar()))
                .collect(Collectors.toList());

        return BeverageSizeDetailsWithRecommendDTO.builder()
                .id(size.getId())
                .sizeType(size.getSizeType())
                .sizeTypeDetail(size.getSizeTypeDetail())
                .volume(size.getVolume())
                .sugar((int) size.getSugar())
                .calories((int) size.getCalories())
                .caffeine((int) size.getCaffeine())
                .recommends(recommends)
                .build();
    }

    private RecommendedBeverageDTO convertToRecommendedBeverageDTO(BeverageSize size, double baseSugar) {
        Beverage beverage = size.getBeverage();
        double sugarGap = size.getSugar() - baseSugar;
        double roundedSugarGap = Math.round(sugarGap * 10.0) / 10.0;

        return RecommendedBeverageDTO.builder()
                .beverageId(beverage.getBeverageId())
                .name(beverage.getName())
                .brand(beverage.getBrand())
                .imgUrl(beverage.getImgUrl())
                .sizeType(size.getSizeType())
                .sizeTypeDetail(size.getSizeTypeDetail())
                .volume(size.getVolume())
                .sugarGap(roundedSugarGap)
                .build();
    }

    public Optional<Beverage> findBeverageByBeverageId(Long beverageId) {
        return beverageRepository.findById(beverageId);
    }

    public List<InnerListBeverageDTO> findBeveragesByFilters(
            String brand, String category, String keyword, String sort
    ) {
        Specification<Beverage> spec = buildSpecification(brand, category, keyword);
        Sort sortOrder = resolveSort(sort);

        return beverageRepository.findAll(spec, sortOrder)
                .stream()
                .map(this::convertToInnerListBeverageDTO)
                .collect(Collectors.toList());
    }

    private Specification<Beverage> buildSpecification(String brand, String category, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(brand)) {
                predicates.add(cb.equal(root.get("brand"), brand));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"),
                        BeverageCategory.valueOf(category)));
            }
            if (StringUtils.hasText(keyword)) {
                predicates.add(cb.like(root.get("name"), "%" + keyword + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort resolveSort(String sort) {
        return switch (sort.toLowerCase()) {
            case "sugarasc" -> Sort.by(Sort.Order.asc("sugar"));
            case "sugardesc" -> Sort.by(Sort.Order.desc("sugar"));
            default -> Sort.by(Sort.Order.asc("name"));
        };
    }

    private InnerListBeverageDTO convertToInnerListBeverageDTO(Beverage beverage) {
        return InnerListBeverageDTO.builder()
                .beverageId(beverage.getBeverageId())
                .name(beverage.getName())
                .brand(beverage.getBrand())
                .imgUrl(beverage.getImgUrl())
                .sugarPer100ml((int) Math.round(beverage.getSugar()))
                .build();
    }
}
