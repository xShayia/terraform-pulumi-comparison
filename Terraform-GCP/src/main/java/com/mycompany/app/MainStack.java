package com.mycompany.app;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.hashicorp.cdktf.AssetType;
import com.hashicorp.cdktf.TerraformAsset;
import com.hashicorp.cdktf.TerraformAssetConfig;
import com.hashicorp.cdktf.providers.docker.image.Image;
import com.hashicorp.cdktf.providers.docker.image.ImageBuild;
import com.hashicorp.cdktf.providers.docker.image.ImageConfig;
import com.hashicorp.cdktf.providers.docker.provider.DockerProvider;
import com.hashicorp.cdktf.providers.docker.provider.DockerProviderRegistryAuth;
import com.hashicorp.cdktf.providers.docker.registry_image.RegistryImage;
import com.hashicorp.cdktf.providers.docker.registry_image.RegistryImageConfig;
import com.hashicorp.cdktf.providers.google.artifact_registry_repository.ArtifactRegistryRepository;
import com.hashicorp.cdktf.providers.google.artifact_registry_repository.ArtifactRegistryRepositoryConfig;
import com.hashicorp.cdktf.providers.google.cloud_run_service_iam_binding.CloudRunServiceIamBinding;
import com.hashicorp.cdktf.providers.google.cloud_run_service_iam_binding.CloudRunServiceIamBindingConfig;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2Service;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceConfig;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplate;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainers;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainersEnv;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplateContainersPorts;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplateVpcAccess;
import com.hashicorp.cdktf.providers.google.cloud_run_v2_service.CloudRunV2ServiceTemplateVpcAccessNetworkInterfaces;
import com.hashicorp.cdktf.providers.google.compute_backend_bucket.ComputeBackendBucket;
import com.hashicorp.cdktf.providers.google.compute_backend_bucket.ComputeBackendBucketConfig;
import com.hashicorp.cdktf.providers.google.compute_backend_service.ComputeBackendService;
import com.hashicorp.cdktf.providers.google.compute_backend_service.ComputeBackendServiceBackend;
import com.hashicorp.cdktf.providers.google.compute_backend_service.ComputeBackendServiceConfig;
import com.hashicorp.cdktf.providers.google.compute_global_address.ComputeGlobalAddress;
import com.hashicorp.cdktf.providers.google.compute_global_address.ComputeGlobalAddressConfig;
import com.hashicorp.cdktf.providers.google.compute_global_forwarding_rule.ComputeGlobalForwardingRule;
import com.hashicorp.cdktf.providers.google.compute_global_forwarding_rule.ComputeGlobalForwardingRuleConfig;
import com.hashicorp.cdktf.providers.google.compute_network.ComputeNetwork;
import com.hashicorp.cdktf.providers.google.compute_network.ComputeNetworkConfig;
import com.hashicorp.cdktf.providers.google.compute_network_endpoint_group.ComputeNetworkEndpointGroup;
import com.hashicorp.cdktf.providers.google.compute_network_endpoint_group.ComputeNetworkEndpointGroupConfig;
import com.hashicorp.cdktf.providers.google.compute_network_peering.ComputeNetworkPeering;
import com.hashicorp.cdktf.providers.google.compute_network_peering.ComputeNetworkPeeringConfig;
import com.hashicorp.cdktf.providers.google.compute_network_peering_routes_config.ComputeNetworkPeeringRoutesConfig;
import com.hashicorp.cdktf.providers.google.compute_network_peering_routes_config.ComputeNetworkPeeringRoutesConfigConfig;
import com.hashicorp.cdktf.providers.google.compute_region_network_endpoint_group.ComputeRegionNetworkEndpointGroup;
import com.hashicorp.cdktf.providers.google.compute_region_network_endpoint_group.ComputeRegionNetworkEndpointGroupCloudRun;
import com.hashicorp.cdktf.providers.google.compute_region_network_endpoint_group.ComputeRegionNetworkEndpointGroupConfig;
import com.hashicorp.cdktf.providers.google.compute_subnetwork.ComputeSubnetwork;
import com.hashicorp.cdktf.providers.google.compute_subnetwork.ComputeSubnetworkConfig;
import com.hashicorp.cdktf.providers.google.compute_target_http_proxy.ComputeTargetHttpProxy;
import com.hashicorp.cdktf.providers.google.compute_target_http_proxy.ComputeTargetHttpProxyConfig;
import com.hashicorp.cdktf.providers.google.compute_url_map.ComputeUrlMap;
import com.hashicorp.cdktf.providers.google.compute_url_map.ComputeUrlMapConfig;
import com.hashicorp.cdktf.providers.google.compute_url_map.ComputeUrlMapHostRule;
import com.hashicorp.cdktf.providers.google.compute_url_map.ComputeUrlMapPathMatcher;
import com.hashicorp.cdktf.providers.google.compute_url_map.ComputeUrlMapPathMatcherPathRule;
import com.hashicorp.cdktf.providers.google.provider.GoogleProvider;
import com.hashicorp.cdktf.providers.google.service_networking_connection.ServiceNetworkingConnection;
import com.hashicorp.cdktf.providers.google.service_networking_connection.ServiceNetworkingConnectionConfig;
import com.hashicorp.cdktf.providers.google.sql_database.SqlDatabase;
import com.hashicorp.cdktf.providers.google.sql_database.SqlDatabaseConfig;
import com.hashicorp.cdktf.providers.google.sql_database_instance.SqlDatabaseInstance;
import com.hashicorp.cdktf.providers.google.sql_database_instance.SqlDatabaseInstanceConfig;
import com.hashicorp.cdktf.providers.google.sql_database_instance.SqlDatabaseInstanceSettings;
import com.hashicorp.cdktf.providers.google.sql_database_instance.SqlDatabaseInstanceSettingsIpConfiguration;
import com.hashicorp.cdktf.providers.google.sql_user.SqlUser;
import com.hashicorp.cdktf.providers.google.sql_user.SqlUserConfig;
import com.hashicorp.cdktf.providers.google.storage_bucket.StorageBucket;
import com.hashicorp.cdktf.providers.google.storage_bucket.StorageBucketConfig;
import com.hashicorp.cdktf.providers.google.storage_bucket_iam_binding.StorageBucketIamBinding;
import com.hashicorp.cdktf.providers.google.storage_bucket_iam_binding.StorageBucketIamBindingConfig;
import com.hashicorp.cdktf.providers.google.storage_bucket_object.StorageBucketObject;
import com.hashicorp.cdktf.providers.google.storage_bucket_object.StorageBucketObjectConfig;
import software.constructs.Construct;

import com.hashicorp.cdktf.TerraformStack;

public class MainStack extends TerraformStack {
	public MainStack(final Construct scope, final String id) {
		super(scope, id);

		String usernameDB = "user";
		String passDB = "useruser";
		String nameDB = "mydb";
		var region = "europe-central2";
		var project = "constant-tracer-426820-h2";

		GoogleProvider.Builder.create(this, "Google")
				.project(project)
				.region(region)
				.build();


		var bucket = new StorageBucket(this, "my-bucket",
				StorageBucketConfig.builder()
						.name("pulumi-test-124-bucket")
						.location("EU")
						.build());

		var bucketBinding = new StorageBucketIamBinding(this, "my-bucket-binding", StorageBucketIamBindingConfig.builder()
				.bucket(bucket.getName())
				.role("roles/storage.objectViewer")
				.members(List.of("allUsers"))
				.build());

		try {
			var files = Files.list(Paths.get("C:\\Users\\alitu\\IdeaProjects\\terraform-pulumi-comparison\\frontend\\dist\\demo-app\\browser"))
					.filter(file -> !Files.isDirectory(file))
					.toList();
			files.forEach(file -> {
				var asset = new TerraformAsset(this, file.getFileName()
						.toString() + "asset", TerraformAssetConfig.builder()
						.path(file.toString())
						.type(AssetType.FILE)
						.build()
				);
				var contentType = "";
				try {
					contentType = Files.probeContentType(file);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
				new StorageBucketObject(this, file.getFileName()
						.toString() + "s3File", StorageBucketObjectConfig.builder()
						.bucket(bucket.getId())
						.name(file.getFileName().toString())
						.source(asset.getPath())
						.contentType(contentType)
						.build()
				);
			});
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		var vpcNetwork = new ComputeNetwork(this, "vpcNetwork", ComputeNetworkConfig.builder()
				.name("vpc-network1")
				.autoCreateSubnetworks(false)
				.build());

		var subnetwork = new ComputeSubnetwork(this, "network-gcp", ComputeSubnetworkConfig.builder()
				.region(region)
				.ipCidrRange("10.0.0.0/16")
				.network(vpcNetwork.getId())
				.name("sub1")
				.build());

		var globalAddress = new ComputeGlobalAddress(this, "google_compute_global_address", ComputeGlobalAddressConfig.builder()
				.name("googlecomputeglobaladdress2")
				.purpose("VPC_PEERING")
				.addressType("INTERNAL")
				.prefixLength(16)
				.network(vpcNetwork.getId())
				.build());

		var connection = new ServiceNetworkingConnection(this, "default", ServiceNetworkingConnectionConfig.builder()
				.network(vpcNetwork.getId())
				.service("servicenetworking.googleapis.com")
				.reservedPeeringRanges(List.of(globalAddress.getName()))
				.build());

		var instance = new SqlDatabaseInstance(this, "instance", SqlDatabaseInstanceConfig.builder()
				.name("my-database-instance2")
				.region(region)
				.databaseVersion("MYSQL_8_0")
				.settings(SqlDatabaseInstanceSettings.builder()
						.tier("db-f1-micro")
						.ipConfiguration(SqlDatabaseInstanceSettingsIpConfiguration.builder()
								.ipv4Enabled(false)
								.privateNetwork(vpcNetwork.getId())
								.build())
						.build())
				.build()
		);

		var databaseGcp = new SqlDatabase(this, "database", SqlDatabaseConfig.builder()
				.name(nameDB)
				.instance(instance.getName())
				.build());

		var user = new SqlUser(this, "user", SqlUserConfig.builder()
				.name(usernameDB)
				.instance(instance.getName())
				.host("%")
				.password(passDB)
				.build());

		var networkPeeringRoutesConfig = new ComputeNetworkPeeringRoutesConfig(this, "networkPeeringRoutesConfig", ComputeNetworkPeeringRoutesConfigConfig.builder()
				.peering(connection.getPeering())
				.network(vpcNetwork.getName())
				.importCustomRoutes(true)
				.exportCustomRoutes(true)
				.build());

		var repoDockerGcp = new ArtifactRegistryRepository(this, "my-repo", ArtifactRegistryRepositoryConfig.builder()
				.location(region)
				.repositoryId("repodockergcp1")
				.format("DOCKER")
				.build());

		var registry = region + "-docker.pkg.dev";
		DockerProvider.Builder.create(this, "docker")
				.registryAuth(List.of(
						DockerProviderRegistryAuth.builder()
								.address(registry)
								.build()
				))
				.build();

		Image image = new Image(this, "image", ImageConfig
				.builder()
				.name(registry + "/" + project + "/" + repoDockerGcp.getName() + "/myimg")
				.buildAttribute(ImageBuild.builder()
						.context("C:\\Users\\alitu\\IdeaProjects\\terraform-pulumi-comparison\\backend").build()
				)
				.triggers(Map.of("1", UUID.randomUUID().toString()))
				.build());


		RegistryImage registryImage = new RegistryImage(this, "registryImage", RegistryImageConfig.builder()
				.name(image.getName())
				.keepRemotely(true)
				.triggers(Map.of("1", image.getRepoDigest()))
				.build());

		var cloudRunService = new CloudRunV2Service(this, "cloudrun", CloudRunV2ServiceConfig.builder()
				.name("cloudrun-service1")
				.location(region)
				.launchStage("GA")
				.template(CloudRunV2ServiceTemplate.builder()
						.containers(List.of(
								CloudRunV2ServiceTemplateContainers.builder()
										.image(image.getName() + "@" + registryImage.getSha256Digest())
										.env(List.of(
												CloudRunV2ServiceTemplateContainersEnv.builder()
														.name("MYSQL_HOST")
														.value(instance.getFirstIpAddress())
														.build(),
												CloudRunV2ServiceTemplateContainersEnv.builder()
														.name("MYSQL_USER")
														.value(usernameDB)
														.build(),
												CloudRunV2ServiceTemplateContainersEnv.builder()
														.name("MYSQL_PASS")
														.value(passDB)
														.build(),
												CloudRunV2ServiceTemplateContainersEnv.builder()
														.name("MYSQL_DB")
														.value(nameDB)
														.build()
										))
										.ports(CloudRunV2ServiceTemplateContainersPorts.builder().containerPort(8080).build())
										.build()
						))
						.vpcAccess(
								CloudRunV2ServiceTemplateVpcAccess.builder()
										.networkInterfaces(
												List.of(
														CloudRunV2ServiceTemplateVpcAccessNetworkInterfaces.builder()
																.network(vpcNetwork.getId())
																.subnetwork(subnetwork.getId())
																.build()
												)
										)
										.build()
						)
						.build())
				.build());

		var iamBinding = new CloudRunServiceIamBinding(this, "iamBinding", CloudRunServiceIamBindingConfig.builder()
				.location(region)
				.service(cloudRunService.getName())
				.role("roles/run.invoker")
				.members(List.of("allUsers"))
				.build());

		var ipaddress = new ComputeGlobalAddress(this, "my-lb-ipaddress", ComputeGlobalAddressConfig.builder()
				.addressType("EXTERNAL")
				.name("terraformip")
				.build());

		var endpointGroup = new ComputeRegionNetworkEndpointGroup(this,"my-lb-epg", ComputeRegionNetworkEndpointGroupConfig.builder()
				.name("endpointgrpup")
				.networkEndpointType("SERVERLESS")
				.region(region)
				.cloudRun(ComputeRegionNetworkEndpointGroupCloudRun.builder().service(cloudRunService.getName()).build())
				.build());

		var service = new ComputeBackendService(this, "my-lb-backend-service", ComputeBackendServiceConfig.builder()
				.name("backendservice")
				.enableCdn(false)
				.connectionDrainingTimeoutSec(10)
				.backend(List.of(ComputeBackendServiceBackend.builder().group(endpointGroup.getId()).build()))
				.build());

		var bucketGcpLB = new ComputeBackendBucket(this, "bucketGcpLB", ComputeBackendBucketConfig.builder()
				.name("bucketgcplbhhjdf")
				.bucketName(bucket.getName())
				.build());

		var httpsPaths = new ComputeUrlMap(this, "httpsPaths", ComputeUrlMapConfig.builder()
				.name("httppaths")
				.defaultService(bucketGcpLB.getId())
				.hostRule(List.of(ComputeUrlMapHostRule.builder().hosts(List.of("*")).pathMatcher("all-paths").build()))
				.pathMatcher(List.of(
						ComputeUrlMapPathMatcher.builder()
								.name("all-paths")
								.defaultService(service.getId())
								.pathRule(List.of(
										ComputeUrlMapPathMatcherPathRule.builder()
												.paths(List.of("/visit"))
												.service(service.getId())
												.build(),
										ComputeUrlMapPathMatcherPathRule.builder()
												.paths(List.of("/*"))
												.service(bucketGcpLB.getId())
												.build()
								))
								.build()
				))
				.build());


		var httpProxy = new ComputeTargetHttpProxy(this, "my-lb-http-proxy", ComputeTargetHttpProxyConfig.builder()
				.name("httpproxy")
				.urlMap(httpsPaths.getSelfLink())
				.build());


		var globalForwardingRule = new ComputeGlobalForwardingRule(this, "y-lb-http", ComputeGlobalForwardingRuleConfig.builder()
				.name("forwadingrule")
				.target(httpProxy.getSelfLink())
				.ipAddress(ipaddress.getAddress())
				.portRange("80")
				.loadBalancingScheme("EXTERNAL")
				.build());
	}
}