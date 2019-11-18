# Change Log

## 0.3.1

### Changes

#### NEW: Item pages

- Individual items now have their own pages
- These pages include the item's history

## 0.3.0

### Changes

#### NEW: Gold currency

- Player can now earn and keep track of gold currency

#### NEW: Market

- Ability to buy and sell items using gold currency
- Some items exclusive to market

#### NEW: Mine backgrounds

- Each mine type has a unique background tileset

#### NEW: Item sprites

- Every item now has icons of 3 sizes (16x16, 32x32, 64x64)

#### NEW: Items

- Ice Pick

#### Massive transformation to backend-only server

- Remove all static pages
- Bearer token-based authentication for most endpoints
- Login/register now done through API
- Add inventory to equipment change response
- Add username to user response
- Add mine & mining level to mine action response

#### Miscellaneous

- Complete remake of item system from scratch: better for dex, market, inventory, and future features
- New player now starts with one Ice Pick instead of the three existing pickaxe types, those must now be bought

### Bug fixes 

- Mine generation now fails if already in a mine 

## 0.2.0

### Changes

- Initial mobile support
- Upgrade unit dex with new style, information, functionality
- Add defence stat to units, adjust damage formula to take it into account
- Improve copper mine sprite
- Highlight current site section in navigation bar
- **NEW UNIT**: Flamango

### Bug fixes 

- Fix mining results list not expanding properly when exiting mine 

## 0.1.0

- Initial release
