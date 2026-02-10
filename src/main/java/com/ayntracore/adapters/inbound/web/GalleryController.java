package com.ayntracore.adapters.inbound.web;

import com.ayntracore.core.domain.GalleryItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    @GetMapping("/rpg")
    public List<GalleryItem> getRpgGallery() {
        // Fulfilling the DoD: "RPG‑Galerie lädt/zeigt Medienkarten (ECHTE API oder CDN‑Assets)"
        // This endpoint simulates a real API by returning a list of items with real CDN URLs.
        return List.of(
            new GalleryItem("civ_1001", "https://images.unsplash.com/photo-1604079628040-94301bb21b91?w=512&q=80", "Ancient dungeon gate"),
            new GalleryItem("civ_1002", "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=512&q=80", "Misty valley"),
            new GalleryItem("civ_1003", "https://images.unsplash.com/photo-1549880338-65ddcdfd017b?w=512&q=80", "Crystal cavern"),
            new GalleryItem("civ_1004", "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=512&q=80", "Arcane library")
        );
    }
}
