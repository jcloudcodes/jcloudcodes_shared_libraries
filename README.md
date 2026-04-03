#Phase 1 goal
Build a library that lets each repo use a very small Jenkinsfile like this:

@Library('jcloudcodes-shared-library@main') _
pipelineEntry()

Step 1: Decide the shared pipeline flow

For both Django and Java, use this common flow:

Checkout
Init
Build/Test
Sonar scan
Package artifact
Upload artifact to Nexus
Build/push Docker image
GitOps deploy
ArgoCD wait
Post cleanup

Only Build/Test and maybe Package artifact change by project type.



#First groovy first
Start with these vars/ files first,Build these first, in this order:
1. loadProjectConfig.groovy
    This reads ci/project.yaml.
    It should be created inside each application repo (Django or Java project): ci/project.yaml = project-specific configuration (differs per app)
    Example: Django repo

    django-app/
     ├── Jenkinsfile
     ├── ci/
     │   └── project.yaml   ✅ HERE
     ├── manage.py
     ├── requirements.txt

     Example: Java repo
     java-app/
      ├── Jenkinsfile
      ├── ci/
      │   └── project.yaml   ✅ HERE
      ├── pom.xml
2. commonCheckout.groovy:
