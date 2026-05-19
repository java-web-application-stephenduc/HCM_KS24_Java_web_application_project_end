---
name: Academic Excellence System
colors:
  surface: '#f7f9fb'
  surface-dim: '#d8dadc'
  surface-bright: '#f7f9fb'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4f6'
  surface-container: '#eceef0'
  surface-container-high: '#e6e8ea'
  surface-container-highest: '#e0e3e5'
  on-surface: '#191c1e'
  on-surface-variant: '#45464d'
  inverse-surface: '#2d3133'
  inverse-on-surface: '#eff1f3'
  outline: '#76777d'
  outline-variant: '#c6c6cd'
  surface-tint: '#565e74'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#131b2e'
  on-primary-container: '#7c839b'
  inverse-primary: '#bec6e0'
  secondary: '#0058be'
  on-secondary: '#ffffff'
  secondary-container: '#2170e4'
  on-secondary-container: '#fefcff'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#0b1c30'
  on-tertiary-container: '#75859d'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dae2fd'
  primary-fixed-dim: '#bec6e0'
  on-primary-fixed: '#131b2e'
  on-primary-fixed-variant: '#3f465c'
  secondary-fixed: '#d8e2ff'
  secondary-fixed-dim: '#adc6ff'
  on-secondary-fixed: '#001a42'
  on-secondary-fixed-variant: '#004395'
  tertiary-fixed: '#d3e4fe'
  tertiary-fixed-dim: '#b7c8e1'
  on-tertiary-fixed: '#0b1c30'
  on-tertiary-fixed-variant: '#38485d'
  background: '#f7f9fb'
  on-background: '#191c1e'
  surface-variant: '#e0e3e5'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 8px
  container-max: 1440px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: 32px
---

## Brand & Style

The design system is engineered for "Smart Academic & Lab Support," prioritizing institutional authority, operational efficiency, and data integrity. The brand personality is **distinguished, reliable, and meticulously organized**, aiming to evoke a sense of academic rigor and professional trust.

The chosen style is **Corporate / Modern**, characterized by a highly structured layout, generous but functional whitespace, and a focus on legibility for complex data sets. It avoids unnecessary ornamentation, ensuring that administrative tasks and laboratory data remain the primary focus. The visual language is designed to accommodate the density of enterprise dashboards while maintaining a clean, breathable interface that supports prolonged usage by researchers, students, and administrators.

## Colors

The palette is anchored by **Navy Blue (#0F172A)**, chosen for its traditional association with academic institutions and its ability to convey stability and high-level professionalism. 

- **Primary:** Deep Navy for navigation, primary buttons, and headers.
- **Secondary:** A bright Info Blue for interactive elements, links, and selection states.
- **Surface & Background:** Utilizes a range of light grays (`#F8FAFC`, `#F1F5F9`) to differentiate dashboard cards from the main canvas, reducing cognitive load.
- **Semantic Logic:** Status colors follow industry standards but are slightly desaturated to maintain the "Academic" aesthetic without appearing overly jarring.

All color combinations are tested for AA/AAA accessibility compliance, ensuring that Vietnamese diacritics remain sharp and legible against any background.

## Typography

This design system utilizes **Inter** as its sole typeface. Inter’s tall x-height and neutral character make it exceptionally suited for the Vietnamese language, where diacritics often require additional vertical space to prevent overcrowding.

- **Scale:** A modular scale is used to maintain hierarchy in data-heavy environments. 
- **Readability:** Body text is optimized at 14px (`body-md`) for dashboards to maximize information density without sacrificing legibility. 
- **Vietnamese Support:** All levels are configured with `font-feature-settings` to ensure that Vietnamese characters are rendered with consistent tracking and vertical alignment.
- **Weight Usage:** Bold weights (700) are reserved for page titles, while Semi-bold (600) is used for card headers and data labels to provide clear visual anchors.

## Layout & Spacing

The design system employs a **12-column fixed grid** for desktop environments, centered with a maximum width of 1440px to ensure data rows do not become uncomfortably long.

- **The 8px Rule:** All spacing, padding, and margins are multiples of 8px. This creates a predictable rhythm that simplifies development and ensures a cohesive look across different modules.
- **Dashboard Context:** For sidebars and main content areas, a fluid-width approach is used within the 1440px constraint.
- **Mobile Adaptation:** On screens below 768px, the layout reflows to a single column with 16px horizontal margins. Complex data tables should implement horizontal scrolling or card-view transformations.

## Elevation & Depth

Visual hierarchy is established through **Tonal Layers** and **Ambient Shadows**. This design system avoids heavy shadows to maintain its "Clean Academic" look.

- **Level 0 (Base):** Used for the main background (Light Gray).
- **Level 1 (Surface):** Used for primary cards and content areas. These feature a white background and a very soft, diffused shadow (`0px 1px 3px rgba(0,0,0,0.1)`).
- **Level 2 (Overlay):** Used for dropdowns and popovers. These utilize a more pronounced shadow to indicate they are floating above the UI.
- **Borders:** Subtle 1px borders in `#E2E8F0` are used on tables and input fields to define boundaries without adding visual weight.

## Shapes

The shape language is **Professional and Standardized**. 

- **Containers & Cards:** Use a 0.5rem (8px) radius to soften the interface while maintaining a structured, enterprise feel.
- **Form Controls:** Inputs and buttons also utilize the 0.5rem radius for consistency.
- **Badges/Status Chips:** These utilize a **Pill-shape** (full round) to clearly distinguish them from interactive buttons and static containers.
- **Tables:** Table corners are rounded only at the outer container level; internal cells maintain sharp 90-degree intersections to preserve grid alignment.

## Components

### Buttons
Primary buttons use the Navy Blue background with white text. Secondary buttons use a ghost style with a Navy border. Use "Large" (48px) for primary actions and "Medium" (40px) for standard dashboard interactions.

### Cards
Cards are the primary container for lab data and academic summaries. They must have a white background, a 1px soft border, and the Level 1 shadow. Headers within cards should have a subtle bottom border.

### Tables
Tables are central to this design system. They must feature:
- Sticky headers for long datasets.
- Alternating row highlights (Zebra striping) using `#F8FAFC`.
- Subtle 1px horizontal dividers.
- High-contrast text for data values and Secondary Blue for actionable IDs (e.g., Student ID, Lab Sample ID).

### Form Controls
Inputs feature a 1px border that shifts to Secondary Blue on focus. Labels must always be visible (no placeholder-only labels) to support the "Academic" clarity requirement.

### Badges & Status
Badges are pill-shaped with a light background tint and a darker text color of the same hue (e.g., Success: Light Green background + Dark Green text). This ensures they are prominent but do not compete with primary buttons.

### Additional Components
- **Data Visualization Wrappers:** Consistent styling for charts and graphs.
- **File Upload Zones:** Dotted border areas for lab report submissions.
- **Breadcrumbs:** Essential for navigating deep academic hierarchies (Department > Course > Lab).