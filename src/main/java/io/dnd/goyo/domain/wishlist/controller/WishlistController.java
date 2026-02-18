package io.dnd.goyo.domain.wishlist.controller;

import io.dnd.goyo.domain.wishlist.dto.request.WishlistAddRequest;
import io.dnd.goyo.domain.wishlist.dto.response.WishCountResponse;
import io.dnd.goyo.domain.wishlist.dto.response.WishlistAddResponse;
import io.dnd.goyo.domain.wishlist.dto.response.WishlistItemResponse;
import io.dnd.goyo.domain.wishlist.service.WishlistService;
import io.dnd.goyo.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Wishlist", description = "찜 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "찜 추가", description = "공간을 찜 목록에 추가합니다.")
    @PostMapping("/wishlists")
    public ResponseEntity<WishlistAddResponse> addWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WishlistAddRequest request
    ) {
        Long wishlistId = wishlistService.addWishlist(userDetails.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(WishlistAddResponse.from(wishlistId));
    }

    @Operation(summary = "내 찜 목록 조회", description = "로그인한 사용자의 찜 목록을 페이지네이션으로 조회합니다.")
    @GetMapping("/wishlists/me")
    public ResponseEntity<Page<WishlistItemResponse>> getMyWishlists(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(wishlistService.getMyWishlists(userDetails.userId(), pageable));
    }

    @Operation(summary = "찜 삭제", description = "공간을 찜 목록에서 삭제합니다.")
    @DeleteMapping("/wishlists/places/{placeId}")
    public ResponseEntity<Void> removeWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long placeId
    ) {
        wishlistService.removeWishlist(userDetails.userId(), placeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "공간 찜 수 조회", description = "특정 공간의 찜 수를 조회합니다.")
    @GetMapping("/places/{placeId}/wish-count")
    public ResponseEntity<WishCountResponse> getWishCount(@PathVariable Long placeId) {
        return ResponseEntity.ok(wishlistService.getWishCount(placeId));
    }
}
