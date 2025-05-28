@Repository
@RequiredArgsConstructor
public class JpaProductDocumentParametersRepositoryImpl implements ProductDocumentParametersRepository {

    private final SpringJpaProductDocumentParametersRepository repository;

    @Override
    public List<ProductDocumentParameters> findProductDocumentParameters(String entity, String productId) {
        return repository.findByProduct(entity, productId).stream()
                .map(ProductDocumentParametersMapper.INSTANCE::toProductDocumentParameters)
                .toList();
    }
}


@Mapper
public interface ProductDocumentParametersMapper {
    ProductDocumentParametersMapper INSTANCE = Mappers.getMapper(ProductDocumentParametersMapper.class);

    @Mapping(target = "documentalType", source = "documentalTypeDoc")
    @Mapping(target = "documentalTypeCode", source = "documentalCodeDoc")
    ProductDocumentParameters toProductDocumentParameters(ProductDocumentParametersModel productDocumentParametersModel);
}

public interface ProductDocumentParametersRepository {
    List<ProductDocumentParameters> findProductDocumentParameters(String entity, String productId);
}

@Controller
@RequiredArgsConstructor
public class ProductDocumentParametersRSocketController implements ProductDocumentParametersRepositoryClient{
    private final ProductDocumentParametersRepository repository;

    @Override
    public Flux<ProductDocumentParameters> findProductDocumentParameters(ProductDocumentParametersRequest request) {
        return Flux.fromIterable(repository.findProductDocumentParameters(request.getEntity(), request.getProductId()));
    }
}

public interface SpringJpaProductDocumentParametersRepository  extends CrudRepository<ProductDocumentParametersModel, Long> {
    List<ProductDocumentParametersModel> findByProduct(String entity, String product);
}


Error starting ApplicationContext. To display the conditions report re-run your application with 'debug' enabled.
2025-05-28 14:02:56.540 ERROR [conac-springboot-fx-db,,] 19436 --- [           main] o.s.boot.SpringApplication               : Application run failed

org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'jpaProductDocumentParametersRepositoryImpl' defined in file [C:\GIT_REPOS\acelera\conac-springboot-fx-db\target\classes\com\acelera\fx\db\infrastructure\adapter\persistence\jpa\repository\JpaProductDocumentParametersRepositoryImpl.class]: Unsatisfied dependency expressed through constructor parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'springJpaProductDocumentParametersRepository' defined in com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository defined in @EnableJpaRepositories declared on JpaRepositoriesRegistrar.EnableJpaRepositoriesConfiguration: Invocation of init method failed; nested exception is org.springframework.data.repository.query.QueryCreationException: Could not create query for public abstract java.util.List com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository.findByProduct(java.lang.String,java.lang.String); Reason: Failed to create query for method public abstract java.util.List com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository.findByProduct(java.lang.String,java.lang.String)! At least 2 parameter(s) provided but only 1 parameter(s) present in query.; nested exception is java.lang.IllegalArgumentException: Failed to create query for method public abstract java.util.List com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository.findByProduct(java.lang.String,java.lang.String)! At least 2 parameter(s) provided but only 1 parameter(s) present in query.
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:801) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:224) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1372) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1222) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:582) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:955) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:929) ~[spring-context-5.3.31.jar:5.3.31]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:591) ~[spring-context-5.3.31.jar:5.3.31]
	at org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext.refresh(ReactiveWebServerApplicationContext.java:66) ~[spring-boot-2.7.18.jar:2.7.18]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:732) ~[spring-boot-2.7.18.jar:2.7.18]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:409) ~[spring-boot-2.7.18.jar:2.7.18]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:308) ~[spring-boot-2.7.18.jar:2.7.18]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1300) ~[spring-boot-2.7.18.jar:2.7.18]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1289) ~[spring-boot-2.7.18.jar:2.7.18]
	at com.acelera.fx.db.FxDbApplication.main(FxDbApplication.java:17) ~[classes/:na]
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'springJpaProductDocumentParametersRepository' defined in com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository defined in @EnableJpaRepositories declared on JpaRepositoriesRegistrar.EnableJpaRepositoriesConfiguration: Invocation of init method failed; nested exception is org.springframework.data.repository.query.QueryCreationException: Could not create query for public abstract java.util.List com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository.findByProduct(java.lang.String,java.lang.String); Reason: Failed to create query for method public abstract java.util.List com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository.findByProduct(java.lang.String,java.lang.String)! At least 2 parameter(s) provided but only 1 parameter(s) present in query.; nested exception is java.lang.IllegalArgumentException: Failed to create query for method public abstract java.util.List com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository.findByProduct(java.lang.String,java.lang.String)! At least 2 parameter(s) provided but only 1 parameter(s) present in query.
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1804) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:620) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:542) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1391) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1311) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:911) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:788) ~[spring-beans-5.3.31.jar:5.3.31]
	... 19 common frames omitted
Caused by: org.springframework.data.repository.query.QueryCreationException: Could not create query for public abstract java.util.List com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository.findByProduct(java.lang.String,java.lang.String); Reason: Failed to create query for method public abstract java.util.List com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository.findByProduct(java.lang.String,java.lang.String)! At least 2 parameter(s) provided but only 1 parameter(s) present in query.; nested exception is java.lang.IllegalArgumentException: Failed to create query for method public abstract java.util.List com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository.findByProduct(java.lang.String,java.lang.String)! At least 2 parameter(s) provided but only 1 parameter(s) present in query.
	at org.springframework.data.repository.query.QueryCreationException.create(QueryCreationException.java:101) ~[spring-data-commons-2.7.18.jar:2.7.18]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.lookupQuery(QueryExecutorMethodInterceptor.java:107) ~[spring-data-commons-2.7.18.jar:2.7.18]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.lambda$mapMethodsToQuery$1(QueryExecutorMethodInterceptor.java:95) ~[spring-data-commons-2.7.18.jar:2.7.18]
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197) ~[na:na]
	at java.base/java.util.Iterator.forEachRemaining(Iterator.java:133) ~[na:na]
	at java.base/java.util.Collections$UnmodifiableCollection$1.forEachRemaining(Collections.java:1061) ~[na:na]
	at java.base/java.util.Spliterators$IteratorSpliterator.forEachRemaining(Spliterators.java:1845) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499) ~[na:na]
	at java.base/java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:921) ~[na:na]
	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234) ~[na:na]
	at java.base/java.util.stream.ReferencePipeline.collect(ReferencePipeline.java:682) ~[na:na]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.mapMethodsToQuery(QueryExecutorMethodInterceptor.java:97) ~[spring-data-commons-2.7.18.jar:2.7.18]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.lambda$new$0(QueryExecutorMethodInterceptor.java:87) ~[spring-data-commons-2.7.18.jar:2.7.18]
	at java.base/java.util.Optional.map(Optional.java:260) ~[na:na]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.<init>(QueryExecutorMethodInterceptor.java:87) ~[spring-data-commons-2.7.18.jar:2.7.18]
	at org.springframework.data.repository.core.support.RepositoryFactorySupport.getRepository(RepositoryFactorySupport.java:365) ~[spring-data-commons-2.7.18.jar:2.7.18]
	at org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport.lambda$afterPropertiesSet$5(RepositoryFactoryBeanSupport.java:323) ~[spring-data-commons-2.7.18.jar:2.7.18]
	at org.springframework.data.util.Lazy.getNullable(Lazy.java:231) ~[spring-data-commons-2.7.18.jar:2.7.18]
	at org.springframework.data.util.Lazy.get(Lazy.java:115) ~[spring-data-commons-2.7.18.jar:2.7.18]
	at org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport.afterPropertiesSet(RepositoryFactoryBeanSupport.java:329) ~[spring-data-commons-2.7.18.jar:2.7.18]
	at org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean.afterPropertiesSet(JpaRepositoryFactoryBean.java:144) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1863) ~[spring-beans-5.3.31.jar:5.3.31]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1800) ~[spring-beans-5.3.31.jar:5.3.31]
	... 30 common frames omitted
Caused by: java.lang.IllegalArgumentException: Failed to create query for method public abstract java.util.List com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository.findByProduct(java.lang.String,java.lang.String)! At least 2 parameter(s) provided but only 1 parameter(s) present in query.
	at org.springframework.data.jpa.repository.query.PartTreeJpaQuery.<init>(PartTreeJpaQuery.java:96) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy$CreateQueryLookupStrategy.resolveQuery(JpaQueryLookupStrategy.java:119) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy$CreateIfNotFoundQueryLookupStrategy.resolveQuery(JpaQueryLookupStrategy.java:259) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy$AbstractQueryLookupStrategy.resolveQuery(JpaQueryLookupStrategy.java:93) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.lookupQuery(QueryExecutorMethodInterceptor.java:103) ~[spring-data-commons-2.7.18.jar:2.7.18]
	... 52 common frames omitted
Caused by: java.lang.IllegalArgumentException: At least 2 parameter(s) provided but only 1 parameter(s) present in query.
	at org.springframework.util.Assert.isTrue(Assert.java:139) ~[spring-core-5.3.31.jar:5.3.31]
	at org.springframework.data.jpa.repository.query.QueryParameterSetterFactory$CriteriaQueryParameterSetterFactory.create(QueryParameterSetterFactory.java:298) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.data.jpa.repository.query.ParameterBinderFactory.createQueryParameterSetter(ParameterBinderFactory.java:140) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.data.jpa.repository.query.ParameterBinderFactory.createSetters(ParameterBinderFactory.java:129) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.data.jpa.repository.query.ParameterBinderFactory.createSetters(ParameterBinderFactory.java:121) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.data.jpa.repository.query.ParameterBinderFactory.createCriteriaBinder(ParameterBinderFactory.java:72) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.data.jpa.repository.query.PartTreeJpaQuery$QueryPreparer.getBinder(PartTreeJpaQuery.java:328) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.data.jpa.repository.query.PartTreeJpaQuery$QueryPreparer.<init>(PartTreeJpaQuery.java:218) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.data.jpa.repository.query.PartTreeJpaQuery$CountQueryPreparer.<init>(PartTreeJpaQuery.java:348) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	at org.springframework.data.jpa.repository.query.PartTreeJpaQuery.<init>(PartTreeJpaQuery.java:91) ~[spring-data-jpa-2.7.18.jar:2.7.18]
	... 56 common frames omitted