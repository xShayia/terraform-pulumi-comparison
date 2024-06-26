package myproject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pulumi.Pulumi;
import com.pulumi.asset.FileAsset;
import com.pulumi.aws.apprunner.Connection;
import com.pulumi.aws.apprunner.ConnectionArgs;
import com.pulumi.aws.apprunner.Service;
import com.pulumi.aws.apprunner.ServiceArgs;
import com.pulumi.aws.apprunner.VpcConnector;
import com.pulumi.aws.apprunner.VpcConnectorArgs;
import com.pulumi.aws.apprunner.inputs.ServiceNetworkConfigurationArgs;
import com.pulumi.aws.apprunner.inputs.ServiceNetworkConfigurationEgressConfigurationArgs;
import com.pulumi.aws.apprunner.inputs.ServiceSourceConfigurationArgs;
import com.pulumi.aws.apprunner.inputs.ServiceSourceConfigurationAuthenticationConfigurationArgs;
import com.pulumi.aws.apprunner.inputs.ServiceSourceConfigurationImageRepositoryArgs;
import com.pulumi.aws.apprunner.inputs.ServiceSourceConfigurationImageRepositoryImageConfigurationArgs;
import com.pulumi.aws.cloudfront.Distribution;
import com.pulumi.aws.cloudfront.DistributionArgs;
import com.pulumi.aws.cloudfront.OriginAccessControl;
import com.pulumi.aws.cloudfront.OriginAccessControlArgs;
import com.pulumi.aws.cloudfront.inputs.DistributionDefaultCacheBehaviorArgs;
import com.pulumi.aws.cloudfront.inputs.DistributionDefaultCacheBehaviorForwardedValuesArgs;
import com.pulumi.aws.cloudfront.inputs.DistributionDefaultCacheBehaviorForwardedValuesCookiesArgs;
import com.pulumi.aws.cloudfront.inputs.DistributionOrderedCacheBehaviorArgs;
import com.pulumi.aws.cloudfront.inputs.DistributionOriginArgs;
import com.pulumi.aws.cloudfront.inputs.DistributionOriginCustomOriginConfigArgs;
import com.pulumi.aws.cloudfront.inputs.DistributionRestrictionsArgs;
import com.pulumi.aws.cloudfront.inputs.DistributionRestrictionsGeoRestrictionArgs;
import com.pulumi.aws.cloudfront.inputs.DistributionViewerCertificateArgs;
import com.pulumi.aws.ec2.InternetGateway;
import com.pulumi.aws.ec2.InternetGatewayArgs;
import com.pulumi.aws.ec2.RouteTable;
import com.pulumi.aws.ec2.RouteTableArgs;
import com.pulumi.aws.ec2.RouteTableAssociation;
import com.pulumi.aws.ec2.RouteTableAssociationArgs;
import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.SecurityGroupArgs;
import com.pulumi.aws.ec2.Subnet;
import com.pulumi.aws.ec2.SubnetArgs;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.aws.ec2.VpcArgs;
import com.pulumi.aws.ec2.inputs.RouteTableRouteArgs;
import com.pulumi.aws.ec2.inputs.SecurityGroupEgressArgs;
import com.pulumi.aws.ec2.inputs.SecurityGroupIngressArgs;
import com.pulumi.aws.ec2.outputs.RouteTableRoute;
import com.pulumi.aws.ec2.outputs.SecurityGroupEgress;
import com.pulumi.aws.ec2.outputs.SecurityGroupIngress;
import com.pulumi.aws.ecr.EcrFunctions;
import com.pulumi.aws.ecr.Repository;
import com.pulumi.aws.ecr.RepositoryArgs;
import com.pulumi.aws.ecr.inputs.GetAuthorizationTokenArgs;
import com.pulumi.aws.ecr.inputs.RepositoryImageScanningConfigurationArgs;
import com.pulumi.aws.ecr.outputs.GetAuthorizationTokenResult;
import com.pulumi.aws.iam.Role;
import com.pulumi.aws.iam.RoleArgs;
import com.pulumi.aws.rds.Instance;
import com.pulumi.aws.rds.InstanceArgs;
import com.pulumi.aws.rds.SubnetGroup;
import com.pulumi.aws.rds.SubnetGroupArgs;
import com.pulumi.aws.s3.BucketObject;
import com.pulumi.aws.s3.BucketObjectArgs;
import com.pulumi.core.Output;
import com.pulumi.aws.s3.Bucket;
import com.pulumi.docker.Image;
import com.pulumi.docker.ImageArgs;
import com.pulumi.docker.inputs.DockerBuildArgs;
import com.pulumi.docker.inputs.RegistryArgs;

import static com.pulumi.codegen.internal.Serialization.jsonArray;
import static com.pulumi.codegen.internal.Serialization.jsonObject;
import static com.pulumi.codegen.internal.Serialization.jsonProperty;
import static com.pulumi.codegen.internal.Serialization.serializeJson;

public class App {
	public static void main(String[] args) {
		Pulumi.run(ctx -> {

			String s3originId = "S3originId";
			String appRunnerOriginId = "AppRunneroriginId";

			String usernameDB = "user";
			String passDB = "useruser";
			String nameDB = "mydb";


			var bucket = new Bucket("frontend-static-pulumi");

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
							.key(file.getFileName().toString())
							.source(new FileAsset(file.toString()))
							.contentType(contentType)
							.build()
					);
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			var originAccessControl = new OriginAccessControl("originAccessControlId", OriginAccessControlArgs.builder()
					.name("originAccessControlId")
					.originAccessControlOriginType("s3")
					.signingBehavior("always")
					.signingProtocol("sigv4")
					.build());


			var ecrRepo = new Repository("ecrRepo", RepositoryArgs.builder()
					.name("ecrrepo_pulumi")
					.imageTagMutability("MUTABLE")
					.imageScanningConfiguration(RepositoryImageScanningConfigurationArgs.builder()
							.scanOnPush(true)
							.build())
					.build());

			final var authToken = EcrFunctions.getAuthorizationToken(GetAuthorizationTokenArgs.builder() //token autentificare de la aws pt docker
					.registryId(ecrRepo.registryId())
					.build());

			var image = new Image("image_pulumi", ImageArgs.builder() //build and push image
					.build(DockerBuildArgs.builder()
							.context("..\\backend\\")
							.dockerfile("..\\backend\\Dockerfile")
							.platform("linux/amd64")
							.build())
					.imageName(ecrRepo.repositoryUrl())
					.registry(RegistryArgs.builder()
							.username(authToken.asSecret().applyValue(GetAuthorizationTokenResult::userName))
							.password(authToken.asSecret().applyValue(GetAuthorizationTokenResult::password))
							.server(ecrRepo.repositoryUrl())
							.build())
					.build());

			var iamRole = new Role("testRole", RoleArgs.builder()
					.name("iam_role")
					.assumeRolePolicy(serializeJson(
							jsonObject(
									jsonProperty("Version", "2012-10-17"),
									jsonProperty("Statement", jsonArray(jsonObject(
											jsonProperty("Action", "sts:AssumeRole"),
											jsonProperty("Effect", "Allow"),
											jsonProperty("Principal", jsonObject(
													jsonProperty("Service", "build.apprunner.amazonaws.com")
											))
									)))
							)))
					.managedPolicyArns("arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRAccess")
					.build());


			var vpc = new Vpc("vpc",  VpcArgs.builder()
					.cidrBlock("10.0.0.0/16")
					.build());


			List<String> azs = List.of("eu-central-1a", "eu-central-1b", "eu-central-1c");

			List<String> public_subnet_cidrs = List.of("10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24");
			List<String> private_subnet_cidrs = List.of("10.0.4.0/24", "10.0.5.0/24", "10.0.6.0/24");

			//ip distribution to azs
			var cidr_to_azs_public = Map.of(public_subnet_cidrs.get(0), azs.get(0), public_subnet_cidrs.get(1), azs.get(1), public_subnet_cidrs.get(2), azs.get(2));
			var cidr_to_azs_private = Map.of(private_subnet_cidrs.get(0), azs.get(0), private_subnet_cidrs.get(1), azs.get(1), private_subnet_cidrs.get(2), azs.get(2));


			Map<String, Subnet> publicSubnetMap = new HashMap<>();


			public_subnet_cidrs
					.forEach(cidr -> {
						publicSubnetMap.put(cidr, new Subnet("_" + cidr.replace(".", "_")
								.replace("/", "_"), SubnetArgs.builder()
								.cidrBlock(cidr).vpcId(vpc.id()).availabilityZone(cidr_to_azs_public.get(cidr))
								.build()));
					});
			var private_subnet = private_subnet_cidrs.stream()
					.map(cidr -> new Subnet("_" + cidr.replace(".", "_").replace("/", "_"), SubnetArgs.builder()
							.vpcId(vpc.id()).cidrBlock(cidr).availabilityZone(cidr_to_azs_private.get(cidr)).build()))
					.collect(Collectors.toList());

			//conexiune vpc si internet (subnet sa poata accesa internetul)
			InternetGateway internetGateway = new InternetGateway("internetGateway", InternetGatewayArgs.builder()
					.vpcId(vpc.id()).build());

			var routeTable = new RouteTable("routeTable", RouteTableArgs.builder().vpcId(vpc.id())
					.routes(List.of(RouteTableRouteArgs.builder().cidrBlock("0.0.0.0/0").gatewayId(internetGateway.id())
							.build())).build());

			public_subnet_cidrs.forEach(cidr -> new RouteTableAssociation("_" + "_" + cidr.replace(".", "_")
					.replace("/", "_"), RouteTableAssociationArgs.builder().subnetId(publicSubnetMap.get(cidr).id())
					.routeTableId(routeTable.id()).build()));

			var dbSubnetGroup = new SubnetGroup("dbSunetGroup", SubnetGroupArgs.builder().name("dbsub")
					.subnetIds(Output.all(private_subnet.stream().map(Subnet::id).collect(Collectors.toList()))).build());

			SecurityGroup securityGroup = new SecurityGroup("securityGroup", SecurityGroupArgs.builder()
					.ingress(List.of(SecurityGroupIngressArgs.builder().fromPort(0).toPort(0).protocol("-1")
							.cidrBlocks(List.of("0.0.0.0/0")).build()))
					.egress(List.of(SecurityGroupEgressArgs.builder().fromPort(0).toPort(0).protocol("-1")
							.cidrBlocks(List.of("0.0.0.0/0")).build()))
					.vpcId(vpc.id())
					.build());

			var apprunnerVpcConnector = new VpcConnector("apprunnerVpcConnector", VpcConnectorArgs.builder()
					.vpcConnectorName("vpccon").subnets(Output.all(private_subnet.stream().map(Subnet::id).collect(Collectors.toList())))
					.securityGroups(Output.all(List.of(securityGroup.id())))
					.build());

			var dbInstance_pulumi = new Instance("dbinstance", InstanceArgs.builder()
					.allocatedStorage(10)
					.dbName(nameDB)
					.engine("mysql")
					.engineVersion("8.0")
					.instanceClass("db.t3.micro")
					.username(usernameDB)
					.password(passDB)
					.parameterGroupName("default.mysql8.0")
					.skipFinalSnapshot(true)
					.vpcSecurityGroupIds(Output.all(securityGroup.id()))
					.dbSubnetGroupName(dbSubnetGroup.name())
					.build());



			var appRunner_pulumi = new Service("apprunner-pulumi", ServiceArgs.builder()
					.serviceName("appRunnerServicePulumi")
					.sourceConfiguration(ServiceSourceConfigurationArgs.builder()
									.authenticationConfiguration(ServiceSourceConfigurationAuthenticationConfigurationArgs.builder()
											.accessRoleArn(iamRole.arn())
											.build())
									.imageRepository(
											ServiceSourceConfigurationImageRepositoryArgs.builder()
													.imageConfiguration(
															ServiceSourceConfigurationImageRepositoryImageConfigurationArgs.builder()
																	.port("8080")
															.runtimeEnvironmentVariables(dbInstance_pulumi.address().applyValue((adress) -> Map.of("MYSQL_HOST", adress, "MYSQL_USER", usernameDB, "MYSQL_PASS", passDB, "MYSQL_DB", nameDB)))
																	.build()
													)
													.imageIdentifier(image.repoDigest())
													.imageRepositoryType("ECR")
													.build()
									)
									.build()
					)
					.networkConfiguration(ServiceNetworkConfigurationArgs.builder()
							.egressConfiguration(ServiceNetworkConfigurationEgressConfigurationArgs.builder()
									.egressType("VPC").vpcConnectorArn(apprunnerVpcConnector.arn()).build()).build())
					.build());


			var cloudFrontDistribution = new Distribution("cloudFrontDistribution", DistributionArgs.builder()

					.origins(
							DistributionOriginArgs.builder()
									.domainName(bucket.bucketRegionalDomainName())
									.originId(s3originId)
									.originAccessControlId(originAccessControl.id())
									.build(),


							DistributionOriginArgs.builder().domainName(appRunner_pulumi.serviceUrl())
					.originId(appRunnerOriginId)
					.customOriginConfig(DistributionOriginCustomOriginConfigArgs.builder()
							.httpPort(80).httpsPort(443).originProtocolPolicy("https-only")
							.originSslProtocols(List.of("TLSv1.2")).build()).build()
			)
					.enabled(true)
					.orderedCacheBehaviors(
							List.of(DistributionOrderedCacheBehaviorArgs.builder().pathPattern("/visit")
									.allowedMethods(List.of("DELETE",
											"GET",
											"HEAD",
											"OPTIONS",
											"PATCH",
											"POST",
											"PUT"))
									.cachedMethods(List.of("GET", "HEAD"))
									.targetOriginId(appRunnerOriginId).viewerProtocolPolicy("redirect-to-https")
									.cachePolicyId("4135ea2d-6df8-44a3-9df3-4b5a84be39ad")


									.build()))
					.defaultCacheBehavior(DistributionDefaultCacheBehaviorArgs.builder()
							.forwardedValues(DistributionDefaultCacheBehaviorForwardedValuesArgs.builder()
									.queryString(false)
									.cookies(DistributionDefaultCacheBehaviorForwardedValuesCookiesArgs.builder()
											.forward("none")
											.build())
									.build())
							.allowedMethods(List.of(
									"GET",
									"HEAD",
									"OPTIONS",
									"PATCH",
									"POST",
									"PUT",
									"DELETE"))
							.cachedMethods(List.of("GET",
									"HEAD"))
							.targetOriginId(s3originId)
							.viewerProtocolPolicy("redirect-to-https")

							.build())

					.restrictions(DistributionRestrictionsArgs.builder()
							.geoRestriction(DistributionRestrictionsGeoRestrictionArgs.builder().restrictionType("none")
									.build())

							.build())
					.viewerCertificate(DistributionViewerCertificateArgs.builder().cloudfrontDefaultCertificate(true)
							.build())

					.build());

		});





	}

}
