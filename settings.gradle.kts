pluginManagement {
    repositories {
        google {
//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://devrepo.kakao.com/nexus/content/groups/public/")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://devrepo.kakao.com/nexus/content/groups/public/")
    }
}

rootProject.name = "malmungchi"
include(":app")
include(":core")
include(":data")
include(":design")
include(":feature:study")
project(":feature:study").projectDir = file("feature/study")
include(":feature:quiz")
project(":feature:quiz").projectDir = file("feature/quiz")
include(":feature:ai")
project(":feature:ai").projectDir = file("feature/ai")
include(":feature:friend")
project(":feature:friend").projectDir = file("feature/friend")
include(":feature:mypage")
project(":feature:mypage").projectDir = file("feature/mypage")
include(":feature:login")
project(":feature:login").projectDir = file("feature/login")
