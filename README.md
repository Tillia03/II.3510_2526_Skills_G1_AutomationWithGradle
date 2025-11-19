# Gradle Automation — Android Project

This project focuses on applying **Gradle automation concepts** in an Android context rather than developing app logic.  
It demonstrates how to configure **Build Types, Product Flavors, Source Sets**, and how to create **custom Gradle tasks** to automate APK management and verification.

---

## Project Objectives

✔ Configure multiple **Build Types** (debug, release, beta)  
✔ Implement **Product Flavors** (free / paid, minSdk21 / minSdk30)  
✔ Use **Source Sets** for variant-specific code  
✔ Create **custom Gradle tasks** to:
- run tests,
- assemble APKs,
- copy APKs to a folder,
- generate a checksum (.md5),
- compress into a ZIP file.

---

## Gradle Technologies Used

| Feature | Description |
|--------|-------------|
| Build Types | debug, release, beta |
| Product Flavors | free / paid, minSdk21 / minSdk30 |
| Build Variants | freeMinSdk21Debug, paidMinSdk30Release, etc. |
| Source Sets | main, test, androidTest, debug, testDebug |
| Custom Tasks | `copyAndVerifyApks`, `zipApkFolder`, `fullPipeline` |
| Checksum | Detect APK integrity |
| ZIP packaging | Generate distributable archive |

---

## Project Structure

GradleExperiment/
│
├── app/
│ ├── src/
│ │ ├── main/
│ │ ├── debug/
│ │ ├── test/
│ │ └── testDebug/
│ └── build.gradle.kts
├── gradle/
├── settings.gradle.kts
├── build.gradle.kts
└── README.md

---

```kotlin
tasks.register<Copy>("copyAndVerifyApks") {
    val sourceDir = layout.buildDirectory.dir("outputs/apk")
    val destDir = layout.projectDirectory.dir("apk")

    from(sourceDir)
    into(destDir)

    doLast {
        destDir.asFileTree.matching { include("**/*.apk") }.files.forEach { apkFile ->
            ant.invokeMethod("checksum", mapOf("file" to apkFile.path))
            println("APK copied: ${apkFile.name}")
            println("Checksum generated for: ${apkFile.name}")
        }
    }
}
```   

## Run Full Automation Pipeline

In the terminal:

./gradlew fullPipeline


This will:
1️ Run tests
2️ Build all APK variants
3️ Copy APKs
4️ Generate checksum files
5️ Create apk_bundle.zip in /dist

## Example Source Sets (Debug Only File)
class DemoClass {
    fun showMessage() = "This is a debug-only feature!"
}

## GitHub Repository

🔗 https://github.com/Tillia03/II.3510_2526_Skills_G1_AutomationWithGradle

## Author

Léna De Oliveira
