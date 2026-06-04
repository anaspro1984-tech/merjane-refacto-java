# Rapport de tests

Total : **22 tests** — 19 tests unitaires + 3 tests d'intégration, tous au vert.

---

## NormalProductHandler — 4 tests

| Test | Description |
|---|---|
| `whenAvailableGreaterThanZero_thenDecrementsStockAndSaves` | Stock > 0 → le stock est décrémenté de 1 et le produit est sauvegardé. Aucune notification envoyée. |
| `whenLastUnitInStock_thenDecrementsToZeroAndSaves` | Dernier exemplaire en stock (available = 1) → le stock passe à 0 et est sauvegardé. |
| `whenAvailableIsZeroAndLeadTimeGreaterThanZero_thenNotifiesDelay` | Rupture de stock avec un délai de réapprovisionnement positif → produit sauvegardé et notification de délai envoyée. |
| `whenAvailableIsZeroAndLeadTimeIsZero_thenNoActionTaken` | Rupture de stock ET leadTime = 0 → aucune sauvegarde, aucune notification. Comportement silencieux confirmé. |

---

## SeasonalProductHandler — 5 tests

| Test | Description |
|---|---|
| `whenInSeasonAndInStock_thenDecrementsAndSaves` | Produit en saison avec du stock → le stock est décrémenté et sauvegardé. |
| `whenInSeasonOutOfStockAndLeadTimeWithinSeason_thenNotifiesDelay` | En saison mais en rupture, et `now + leadTime` reste avant la fin de saison → notification de délai envoyée. |
| `whenInSeasonOutOfStockAndLeadTimeExceedsSeason_thenNotifiesOutOfStock` | En saison mais en rupture, et `now + leadTime` dépasse la fin de saison → notification de rupture, available mis à 0. |
| `whenSeasonNotStartedYet_thenNotifiesOutOfStock` | La saison n'a pas encore commencé → notification de rupture envoyée immédiatement. |
| `whenSeasonAlreadyEnded_thenNotifiesOutOfStockAndSetsAvailableToZero` | La saison est terminée → notification de rupture et available mis à 0. |

---

## ExpirableProductHandler — 5 tests

| Test | Description |
|---|---|
| `whenInStockAndNotExpired_thenDecrementsAndSaves` | Produit en stock et non expiré → le stock est décrémenté et sauvegardé. |
| `whenExpiresTomorrow_thenDecrementsNormally` | Date d'expiration demain → le produit est encore vendable, le stock est décrémenté normalement. |
| `whenInStockButExpired_thenNotifiesExpirationAndSetsAvailableToZero` | Produit en stock mais expiré → notification d'expiration envoyée, available mis à 0. |
| `whenOutOfStockAndNotExpired_thenNotifiesExpiration` | Rupture de stock sur un produit non expiré → notification d'expiration envoyée et available mis à 0. ⚠️ Comportement actuel : une simple rupture déclenche une notification d'expiration alors que le produit n'est pas expiré. Potentiellement un bug métier à clarifier. |
| `whenOutOfStockAndExpired_thenNotifiesExpiration` | Rupture de stock et produit expiré → notification d'expiration envoyée, available reste à 0. |

---

## OrderService — 4 tests

| Test | Description |
|---|---|
| `whenOrderNotFound_thenThrowsOrderNotFoundException` | Un identifiant de commande inexistant lève une `OrderNotFoundException`, qui retourne un HTTP 404. |
| `whenOrderHasNoItems_thenReturnsOkWithoutCallingHandlers` | Une commande sans articles se termine normalement sans solliciter la factory de handlers. |
| `whenOrderHasNormalProduct_thenDelegatestoNormalHandler` | Une commande avec un produit NORMAL : la factory est appelée pour un handler NORMAL et `handle()` est invoqué sur le produit. |
| `whenOrderHasMultipleProductTypes_thenEachProductIsHandledIndependently` | Une commande avec les trois types (NORMAL, EXPIRABLE, SEASONAL) : chaque produit est délégué à son handler dédié, sans interférence entre eux. |

---

## MyControllerIntegrationTests — 2 tests (mis à jour)

| Test | Description |
|---|---|
| `processOrderShouldReturn200WithAllProductTypes` | Test de bout en bout avec les trois types de produits persistés en H2. `POST /orders/{id}/processOrder` retourne HTTP 200 et la commande reste récupérable. |
| `processOrderWithUnknownId_shouldReturn404` | Appel de l'endpoint avec un identifiant inexistant → HTTP 404 retourné (garde-fou contre la régression `.get()` → `.orElseThrow()`). |

---

## MyUnitTests — 1 test (mis à jour)

| Test | Description |
|---|---|
| `normalProductOutOfStock_shouldNotifyDelay` | Migré depuis l'ancien test de `ProductService`. Produit NORMAL avec available = 0 et leadTime = 15 : vérifie que le produit est sauvegardé et qu'une notification de délai est envoyée avec le bon délai. |

