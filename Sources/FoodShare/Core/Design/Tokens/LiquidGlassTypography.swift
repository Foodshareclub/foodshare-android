//
//  LiquidGlassTypography.swift
//  Foodshare
//
//  Liquid Glass Typography System v26
//

import SwiftUI

#if SKIP
// MARK: - Skip-compatible Font.DesignSystem

/// Standalone namespace for design system typography (Skip cannot nest types in external type extensions)
enum _FontDesignSystem {
    static var displayLarge: Font { Font.system(size: 57, weight: .bold) }
    static var displayMedium: Font { Font.system(size: 45, weight: .bold) }
    static var displaySmall: Font { Font.system(size: 36, weight: .bold) }

    static var headlineLarge: Font { Font.system(size: 32, weight: .semibold) }
    static var headlineMedium: Font { Font.system(size: 28, weight: .semibold) }
    static var headlineSmall: Font { Font.system(size: 24, weight: .semibold) }

    static var titleLarge: Font { Font.system(size: 22, weight: .medium) }
    static var titleMedium: Font { Font.system(size: 16, weight: .medium) }
    static var titleSmall: Font { Font.system(size: 14, weight: .medium) }

    static var bodyLarge: Font { Font.system(size: 16, weight: .regular) }
    static var bodyMedium: Font { Font.system(size: 14, weight: .regular) }
    static var bodySmall: Font { Font.system(size: 12, weight: .regular) }

    static var labelLarge: Font { Font.system(size: 14, weight: .medium) }
    static var labelMedium: Font { Font.system(size: 12, weight: .medium) }
    static var labelSmall: Font { Font.system(size: 11, weight: .medium) }

    static var caption: Font { Font.system(size: 12, weight: .regular) }
    static var captionMedium: Font { Font.system(size: 11, weight: .medium) }
    static var captionSmall: Font { Font.system(size: 10, weight: .regular) }
}

extension Font {
    /// Provides `Font.DesignSystem.xxx` access for Skip by returning the standalone enum metatype
    static var DesignSystem: _FontDesignSystem.Type { _FontDesignSystem.self }
}
#else
extension Font {
    enum DesignSystem {
        // MARK: - Display
        static let displayLarge = Font.system(size: 57, weight: .bold, design: .rounded)
        static let displayMedium = Font.system(size: 45, weight: .bold, design: .rounded)
        static let displaySmall = Font.system(size: 36, weight: .bold, design: .rounded)

        // MARK: - Headline
        static let headlineLarge = Font.system(size: 32, weight: .semibold, design: .rounded)
        static let headlineMedium = Font.system(size: 28, weight: .semibold, design: .rounded)
        static let headlineSmall = Font.system(size: 24, weight: .semibold, design: .rounded)

        // MARK: - Title
        static let titleLarge = Font.system(size: 22, weight: .medium, design: .default)
        static let titleMedium = Font.system(size: 16, weight: .medium, design: .default)
        static let titleSmall = Font.system(size: 14, weight: .medium, design: .default)

        // MARK: - Body
        static let bodyLarge = Font.system(size: 16, weight: .regular, design: .default)
        static let bodyMedium = Font.system(size: 14, weight: .regular, design: .default)
        static let bodySmall = Font.system(size: 12, weight: .regular, design: .default)

        // MARK: - Label
        static let labelLarge = Font.system(size: 14, weight: .medium, design: .default)
        static let labelMedium = Font.system(size: 12, weight: .medium, design: .default)
        static let labelSmall = Font.system(size: 11, weight: .medium, design: .default)

        // MARK: - Caption
        static let caption = Font.system(size: 12, weight: .regular, design: .default)
        static let captionMedium = Font.system(size: 11, weight: .medium, design: .default)
        static let captionSmall = Font.system(size: 10, weight: .regular, design: .default)
    }

    // MARK: - LiquidGlass Alias (for convenience)
    enum LiquidGlass {
        // MARK: - Display
        static let displayLarge = Font.DesignSystem.displayLarge
        static let displayMedium = Font.DesignSystem.displayMedium
        static let displaySmall = Font.DesignSystem.displaySmall

        // MARK: - Headline
        static let headlineLarge = Font.DesignSystem.headlineLarge
        static let headlineMedium = Font.DesignSystem.headlineMedium
        static let headlineSmall = Font.DesignSystem.headlineSmall

        // MARK: - Title
        static let titleLarge = Font.DesignSystem.titleLarge
        static let titleMedium = Font.DesignSystem.titleMedium
        static let titleSmall = Font.DesignSystem.titleSmall

        // MARK: - Body
        static let bodyLarge = Font.DesignSystem.bodyLarge
        static let bodyMedium = Font.DesignSystem.bodyMedium
        static let bodySmall = Font.DesignSystem.bodySmall

        // MARK: - Label
        static let labelLarge = Font.DesignSystem.labelLarge
        static let labelMedium = Font.DesignSystem.labelMedium
        static let labelSmall = Font.DesignSystem.labelSmall

        // MARK: - Caption
        static let caption = Font.DesignSystem.caption
        static let captionMedium = Font.DesignSystem.captionMedium
        static let captionSmall = Font.DesignSystem.captionSmall
    }
}
#endif
