#!/bin/bash
# Gradle Sync Script for Petcare Android Project

echo "🔄 Stopping existing Gradle daemons..."
./gradlew --stop

echo ""
echo "🔄 Syncing Gradle dependencies..."
./gradlew build --refresh-dependencies --no-daemon

echo ""
echo "✅ Gradle sync complete!"
echo ""
echo "📝 Next steps:"
echo "1. Open the project in Android Studio / IntelliJ IDEA"
echo "2. Click 'Sync Project with Gradle Files' in the toolbar"
echo "3. Wait for the sync to complete"
echo "4. All 'Unresolved reference' errors should disappear"

