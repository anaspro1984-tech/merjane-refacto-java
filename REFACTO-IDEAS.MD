# Retour de code review

---

## Controllers

Les controllers doivent rester légers et sans logique métier. Plusieurs problèmes sont à corriger ici.

**Extraction de la logique métier**
La logique de traitement des commandes doit être extraite dans une classe `OrderService` dédiée. Le controller ne doit faire qu'appeler ce service.

**Logging**
L'appel `System.out.println(order)` expose plus de données que nécessaire et n'est pas conforme aux bonnes pratiques OWASP, car il risque de faire fuiter des informations sensibles. Le logging doit se limiter au minimum requis pour le suivi,l'identifiant de la commande suffit.

**Code mort**
Les lignes 40 et 41 sont du code mort : `orderId` est ajouté à une liste qui n'est jamais utilisée. À supprimer.

**Gestion des erreurs**
`or.findById(orderId).get()` lance une `NoSuchElementException` non gérée si la commande n'existe pas,le client reçoit une 500 au lieu d'une 404. Un `.orElseThrow()` avec une exception métier suffit à corriger ça.

**Injection par constructeur**
Le code utilise `@Autowired` directement sur les champs. En dehors du contexte Spring, ces champs sont non initialisés, ce qui complique les tests unitaires. L'injection par constructeur permet de rendre les dépendances `final` et de tester sans Spring.

**Nommage des variables**
`or`, `pr`, `ps` pour les repositories et le service sont illisibles. `orderRepository`, `productRepository`, `productService` évitent toute ambiguïté.

---

## Gestion des types de produits

**Enum ProductType**
Toute la logique de branchement repose sur des comparaisons de chaînes comme `p.getType().equals("NORMAL")`. C'est fragile : une faute de frappe passe à la compilation sans erreur, les IDE ne peuvent pas aider. , il faut introduire un enum `ProductType` avec `NORMAL`, `SEASONAL`, `EXPIRABLE`. Le pattern strategy devient alors naturellement propre,la factory résout le handler avec un simple `switch` sur l'enum.

**Pattern Strategy**
Plutôt que de vérifier `p.getType()` à travers des branches conditionnelles, implémenter un pattern Strategy avec trois classes concrètes : `NormalProductHandler`, `SeasonalProductHandler` et `ExpirableProductHandler`. Chacune exposera une méthode `handleOrder(Product product)` avec un comportement propre à son type de produit. Une `ProductHandlerFactory` se charge de retourner le bon handler selon le type.



## Service de notification

Introduire une interface `INotificationService` pour que `NotificationService` puisse être remplacé par des implémentations alternatives à l'avenir, cela permet une évolution possible vers un message broker comme Kafka ou RabbitMQ.

---

## ProductService

La logique de vérification d'expiration des produits et d'autres contrôles sont actuellement dupliqués entre `ProductService` et le controller. Adopter le pattern Strategy par type de produit permettra de centraliser cette logique et d'éliminer la duplication.

**Bug potentiel dans `handleExpiredProduct`**
La méthode commence par re-vérifier si le produit est disponible et non expiré,mais elle n'est appelée depuis le controller que quand cette condition est déjà fausse. La première branche du `if` ne peut donc jamais s'exécuter dans le flux actuel. C'est soit du code mort, soit une incohérence entre ce que le controller vérifie et ce que le service re-vérifie:
Solution >>  Centraliser la logique dans le handler EXPIRABLE règle ce problème.

---

## Cohérence transactionnelle

Le traitement d'une commande fait plusieurs `save()` sur des produits différents. Si l'un échoue au milieu, les autres sont déjà persistés,on se retrouve avec un état incohérent en base. Annoter la méthode de traitement avec `@Transactional` règle ça.


---

## Repositories: OUT OF SCOPE

`OrderRepository` et `ProductRepository` pourraient être abstraits derrière deux interfaces `IOrderRepository` et `IProductRepository` pour découpler la couche d'accès aux données; mais, Spring Data fournit déjà des interfaces (`JpaRepository`), et Mockito les mocke directement dans les tests. Rajouter une couche d'interfaces supplémentaire dans ce contexte risque d'être de l'over-engineering. Cette abstraction aurait plus de sens dans une architecture hexagonale stricte.

