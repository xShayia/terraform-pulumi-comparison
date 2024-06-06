package com.mycompany.app;

import java.util.List;

import software.constructs.Construct;

import com.hashicorp.cdktf.TerraformStack;


import com.hashicorp.cdktf.providers.docker.provider.DockerProvider;
import com.hashicorp.cdktf.providers.docker.image.Image;
import com.hashicorp.cdktf.providers.docker.container.Container;
import com.hashicorp.cdktf.providers.docker.container.ContainerPorts;

public class MainStack extends TerraformStack
{
    public MainStack(final Construct scope, final String id) {
        super(scope, id);

        DockerProvider.Builder.create(this, "docker")
                .build();
        Image image = Image.Builder.create(this, "nginxImage")
                .name("nginx:latest")
                .keepLocally(false)
                .build();
        Container.Builder.create(this, "nginxContainer")
                .image(image.getName())
                .name("tutorial")
                .ports(List.of(ContainerPorts.builder().internal(80).external(8000).build()))
                .build();
    }
}