package myproject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.pulumi.Pulumi;
import com.pulumi.asset.FileAsset;
import com.pulumi.core.Output;
import com.pulumi.docker.Image;
import com.pulumi.docker.ImageArgs;
import com.pulumi.docker.inputs.DockerBuildArgs;
import com.pulumi.gcp.artifactregistry.Repository;
import com.pulumi.gcp.artifactregistry.RepositoryArgs;
import com.pulumi.gcp.cloudrun.IamBinding;
import com.pulumi.gcp.cloudrun.IamBindingArgs;
import com.pulumi.gcp.cloudrunv2.Service;
import com.pulumi.gcp.cloudrunv2.ServiceArgs;
import com.pulumi.gcp.cloudrunv2.inputs.ServiceTemplateArgs;
import com.pulumi.gcp.cloudrunv2.inputs.ServiceTemplateContainerArgs;
import com.pulumi.gcp.cloudrunv2.inputs.ServiceTemplateContainerEnvArgs;
import com.pulumi.gcp.cloudrunv2.inputs.ServiceTemplateContainerPortArgs;
import com.pulumi.gcp.cloudrunv2.inputs.ServiceTemplateVpcAccessArgs;
import com.pulumi.gcp.cloudrunv2.inputs.ServiceTemplateVpcAccessNetworkInterfaceArgs;
import com.pulumi.gcp.compute.BackendBucket;
import com.pulumi.gcp.compute.BackendBucketArgs;
import com.pulumi.gcp.compute.BackendService;
import com.pulumi.gcp.compute.BackendServiceArgs;
import com.pulumi.gcp.compute.GlobalAddress;
import com.pulumi.gcp.compute.GlobalAddressArgs;
import com.pulumi.gcp.compute.GlobalForwardingRule;
import com.pulumi.gcp.compute.GlobalForwardingRuleArgs;
import com.pulumi.gcp.compute.Network;
import com.pulumi.gcp.compute.NetworkArgs;
import com.pulumi.gcp.compute.NetworkPeeringRoutesConfig;
import com.pulumi.gcp.compute.NetworkPeeringRoutesConfigArgs;
import com.pulumi.gcp.compute.RegionNetworkEndpointGroup;
import com.pulumi.gcp.compute.RegionNetworkEndpointGroupArgs;
import com.pulumi.gcp.compute.Subnetwork;
import com.pulumi.gcp.compute.SubnetworkArgs;
import com.pulumi.gcp.compute.TargetHttpProxy;
import com.pulumi.gcp.compute.TargetHttpProxyArgs;
import com.pulumi.gcp.compute.URLMap;
import com.pulumi.gcp.compute.URLMapArgs;
import com.pulumi.gcp.compute.inputs.BackendServiceBackendArgs;
import com.pulumi.gcp.compute.inputs.RegionNetworkEndpointGroupCloudRunArgs;
import com.pulumi.gcp.compute.inputs.URLMapHostRuleArgs;
import com.pulumi.gcp.compute.inputs.URLMapPathMatcherArgs;
import com.pulumi.gcp.compute.inputs.URLMapPathMatcherPathRuleArgs;
import com.pulumi.gcp.servicenetworking.Connection;
import com.pulumi.gcp.servicenetworking.ConnectionArgs;
import com.pulumi.gcp.sql.Database;
import com.pulumi.gcp.sql.DatabaseArgs;
import com.pulumi.gcp.sql.DatabaseInstance;
import com.pulumi.gcp.sql.DatabaseInstanceArgs;
import com.pulumi.gcp.sql.User;
import com.pulumi.gcp.sql.UserArgs;
import com.pulumi.gcp.sql.inputs.DatabaseInstanceSettingsArgs;
import com.pulumi.gcp.sql.inputs.DatabaseInstanceSettingsIpConfigurationArgs;
import com.pulumi.gcp.storage.Bucket;
import com.pulumi.gcp.storage.BucketArgs;
import com.pulumi.gcp.storage.BucketIAMBinding;
import com.pulumi.gcp.storage.BucketIAMBindingArgs;
import com.pulumi.gcp.storage.BucketObject;
import com.pulumi.gcp.storage.BucketObjectArgs;
import com.pulumi.gcp.storage.inputs.BucketWebsiteArgs;
import com.pulumi.resources.CustomResourceOptions;

public class App {
	public static void main(String[] args) {
		Pulumi.run(ctx -> {

			String usernameDB = "user";
			String passDB = "useruser";
			String nameDB = "mydb";

			var region = "europe-central2";
			var bucket = new Bucket("my-bucket",
					BucketArgs.builder()
							.location("EU")
							.website(BucketWebsiteArgs.builder().mainPageSuffix("index.html").build())
							.build());

			try {
				var files = Files.list(Paths.get("C:\\Users\\alitu\\IdeaProjects\\terraform-pulumi-comparison\\frontend\\dist\\demo-app\\browser"))
						.filter(file -> !Files.isDirectory(file))
						.collect(Collectors.toList());
				files.forEach(file -> {
					var contentType = "";
					try {
						contentType = Files.probeContentType(file);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					new BucketObject(file.getFileName().toString() + "asset", BucketObjectArgs.builder()
							.bucket(bucket.id())
							.name(file.getFileName().toString())
							.source(new FileAsset(file.toString()))
							.contentType(contentType)
							.build()
					);
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			var bucketBinding = new BucketIAMBinding("my-bucket-binding", BucketIAMBindingArgs.builder()
					.bucket(bucket.name())
					.role("roles/storage.objectViewer")
					.members("allUsers")
					.build());

			var bucketObject = new BucketObject("googleInfrastructureDiagram", BucketObjectArgs.builder()
					.name("google.png")
					.bucket(bucket.name())
					.source(new FileAsset("../frontend/google.png"))
					.build());

			var vpcNetwork = new Network("vpcNetwork", NetworkArgs.builder()
					.name("vpc-network")
					.autoCreateSubnetworks(false)
					.build());

			var subnetwork = new Subnetwork("network-gcp", SubnetworkArgs.builder()
					.region(region)
					.ipCidrRange("10.0.0.0/16")
					.network(vpcNetwork.id())
					.build());

			GlobalAddress google_compute_global_address = new GlobalAddress("google_compute_global_address", GlobalAddressArgs.builder()
					.name("googlecomputeglobaladdress")
					.purpose("VPC_PEERING")
					.addressType("INTERNAL")
					.prefixLength(16)
					.network(vpcNetwork.id())
					.build());


			var connection = new Connection("default", ConnectionArgs.builder()
					.network(vpcNetwork.id())
					.service("servicenetworking.googleapis.com")
					.reservedPeeringRanges(Output.all(google_compute_global_address.name()))
					.build());


			var instance = new DatabaseInstance("instance", DatabaseInstanceArgs.builder()
					.name("my-database-instance")
					.region(region)
					.databaseVersion("MYSQL_8_0")
					.settings(DatabaseInstanceSettingsArgs.builder()
							.tier("db-f1-micro")
							.ipConfiguration(DatabaseInstanceSettingsIpConfigurationArgs.builder()
									.ipv4Enabled(false)
									.privateNetwork(vpcNetwork.id())
									.build())
							.build())
					.build(), CustomResourceOptions.builder().dependsOn(connection).build()
			);
			var users = new User("users", UserArgs.builder()
					.name(usernameDB)
					.instance(instance.name())
					.host("%")
					.password(passDB)
					.build());

			NetworkPeeringRoutesConfig networkPeeringRoutesConfig = new NetworkPeeringRoutesConfig("networkPeeringRoutesConfig", NetworkPeeringRoutesConfigArgs.builder()
					.peering(connection.peering())
					.network(vpcNetwork.name())
					.importCustomRoutes(true)
					.exportCustomRoutes(true)
					.build());


			var databaseGcp = new Database("database", DatabaseArgs.builder()
					.name(nameDB)
					.instance(instance.name())
					.build());


			var repoDockerGcp = new Repository("my-repo", RepositoryArgs.builder()
					.location(region)
					.repositoryId("repodockergcp")
					.format("DOCKER")
					.build());

			var image = new Image("image_pulumi", ImageArgs.builder() //build and push image
					.build(DockerBuildArgs.builder()
							.context("..\\backend\\")
							.dockerfile("..\\backend\\Dockerfile")
							.platform("linux/amd64")
							.build())
					.imageName("europe-central2-docker.pkg.dev/constant-tracer-426820-h2/repodockergcp/imagedoc")
					.build());


			var cloudrunservice = new Service("default", ServiceArgs.builder()
					.name("cloudrun-service")
					.location(region)
					.launchStage("GA")
					.template(ServiceTemplateArgs.builder()
							.containers(ServiceTemplateContainerArgs.builder()
									.image(image.repoDigest())
									.envs(
											Output.all(
													instance.firstIpAddress()
															.applyValue(address -> ServiceTemplateContainerEnvArgs.builder()
																	.name("MYSQL_HOST")
																	.value(address)
																	.build()),
													Output.of(ServiceTemplateContainerEnvArgs.builder()
															.name("MYSQL_USER")
															.value(usernameDB)
															.build()),
													Output.of(ServiceTemplateContainerEnvArgs.builder()
															.name("MYSQL_PASS")
															.value(passDB)
															.build()),
													Output.of(ServiceTemplateContainerEnvArgs.builder()
															.name("MYSQL_DB")
															.value(nameDB)
															.build())

											))
									.ports(ServiceTemplateContainerPortArgs.builder().containerPort(8080).build())
									.build())
							.vpcAccess(ServiceTemplateVpcAccessArgs.builder()
									.networkInterfaces(ServiceTemplateVpcAccessNetworkInterfaceArgs.builder()
											.network(vpcNetwork.id())
											.subnetwork(subnetwork.id())
											.build())
									.build())

							.build())
					.build(), CustomResourceOptions.builder().dependsOn(databaseGcp, users).build()
			);

			//https://cloud.google.com/run/docs/authenticating/public#terraform
			IamBinding iamBinding = new IamBinding("iamBinding", IamBindingArgs.builder()
					.location(region)
					.service(cloudrunservice.name())
					.role("roles/run.invoker")
					.members("allUsers")
					.build());



            //https://medium.com/develop-everything/create-a-cloud-run-service-and-https-load-balancer-with-pulumi-3ba542e60367
			var ipaddress = new GlobalAddress("my-lb-ipaddress", GlobalAddressArgs.builder()
					.addressType("EXTERNAL")
					.build());

			var endpointGroup = new RegionNetworkEndpointGroup("my-lb-epg", RegionNetworkEndpointGroupArgs.builder()
					.networkEndpointType("SERVERLESS")
					.region(region)
					.cloudRun(RegionNetworkEndpointGroupCloudRunArgs.builder().service(cloudrunservice.name()).build())
					.build());

			var service = new BackendService("my-lb-backend-service", BackendServiceArgs.builder()
					.enableCdn(false)
					.connectionDrainingTimeoutSec(10)
					.backends(List.of(BackendServiceBackendArgs.builder().group(endpointGroup.id()).build()))
					.build());

			BackendBucket bucketGcpLB = new BackendBucket("bucketGcpLB", BackendBucketArgs.builder()
					.name("bucketgcplb")
					.bucketName(bucket.name())
					.build());

			var httpsPaths = new URLMap("httpsPaths", URLMapArgs.builder()
					.defaultService(bucketGcpLB.id())
                    .hostRules(URLMapHostRuleArgs.builder().hosts("*").pathMatcher("all-paths").build())
					.pathMatchers(URLMapPathMatcherArgs.builder().name("all-paths").defaultService(service.id())
                            .pathRules(URLMapPathMatcherPathRuleArgs.builder()
                                    .paths("/visit")
                                    .service(service.id())
                                    .build(),
									URLMapPathMatcherPathRuleArgs.builder()
											.paths("/*")
											.service(bucketGcpLB.id())
											.build()
									)
							.build())
					.build());


            var httpProxy = new TargetHttpProxy("my-lb-http-proxy", TargetHttpProxyArgs.builder()
                    .urlMap(httpsPaths.selfLink())
                    .build());



             var GlobalForwardingRule = new GlobalForwardingRule("y-lb-http", GlobalForwardingRuleArgs.builder()
                     .target(httpProxy.selfLink())
                     .ipAddress(ipaddress.address())
                     .portRange("80")
                     .loadBalancingScheme("EXTERNAL")
                     .build());





		});
	}
}
