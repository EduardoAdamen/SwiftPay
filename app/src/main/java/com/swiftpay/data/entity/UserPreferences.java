// com/swiftpay/data/entity/UserPreferences.java
package com.swiftpay.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_preferences",
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        indices = @Index(value = "user_id", unique = true))
public class UserPreferences {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "user_id")
    private long userId;
    @ColumnInfo(name = "theme_mode", defaultValue = "'SYSTEM'")
    private String themeMode = "SYSTEM";
    @ColumnInfo(name = "color_scheme", defaultValue = "'DEFAULT'")
    private String colorScheme = "DEFAULT";
    @ColumnInfo(name = "font_size", defaultValue = "'NORMAL'")
    private String fontSize = "NORMAL";
    @ColumnInfo(name = "compact_view", defaultValue = "0")
    private int compactView = 0;
    @ColumnInfo(name = "animations_enabled", defaultValue = "1")
    private int animationsEnabled = 1;
    @ColumnInfo(name = "images_enabled", defaultValue = "1")
    private int imagesEnabled = 1;
    @ColumnInfo(name = "notification_sound")
    private String notificationSound;
    @ColumnInfo(name = "wallpaper_path")
    private String wallpaperPath;
    @ColumnInfo(name = "accessibility_mode", defaultValue = "0")
    private int accessibilityMode = 0;

    public UserPreferences() {}
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getThemeMode() { return themeMode; }
    public void setThemeMode(String themeMode) { this.themeMode = themeMode; }
    public String getColorScheme() { return colorScheme; }
    public void setColorScheme(String colorScheme) { this.colorScheme = colorScheme; }
    public String getFontSize() { return fontSize; }
    public void setFontSize(String fontSize) { this.fontSize = fontSize; }
    public int getCompactView() { return compactView; }
    public void setCompactView(int compactView) { this.compactView = compactView; }
    public int getAnimationsEnabled() { return animationsEnabled; }
    public void setAnimationsEnabled(int animationsEnabled) { this.animationsEnabled = animationsEnabled; }
    public int getImagesEnabled() { return imagesEnabled; }
    public void setImagesEnabled(int imagesEnabled) { this.imagesEnabled = imagesEnabled; }
    public String getNotificationSound() { return notificationSound; }
    public void setNotificationSound(String notificationSound) { this.notificationSound = notificationSound; }
    public String getWallpaperPath() { return wallpaperPath; }
    public void setWallpaperPath(String wallpaperPath) { this.wallpaperPath = wallpaperPath; }
    public int getAccessibilityMode() { return accessibilityMode; }
    public void setAccessibilityMode(int accessibilityMode) { this.accessibilityMode = accessibilityMode; }
}
