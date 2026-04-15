#!/bin/bash
# Setup script for deploying generic reports to OpenMRS runtime directory
# This script copies our 114 migrated generic JSON reports to the external directory
# where the Report Builder module will automatically import them on startup.

set -e

echo "========================================"
echo "  Generic Reports Deployment Script"
echo "========================================"
echo ""

# Check if OPENMRS_DATA_DIR environment variable is set
if [ -z "$OPENMRS_DATA_DIR" ]; then
    echo "❌ Error: OPENMRS_DATA_DIR environment variable not set"
    echo ""
    echo "Please set the OPENMRS_DATA_DIR environment variable to point to your OpenMRS application data directory:"
    echo "  export OPENMRS_DATA_DIR=/path/to/openmrs/data"
    echo ""
    echo "Example:"
    echo "  export OPENMRS_DATA_DIR=/var/lib/openmrs"
    echo "  ./setup-generic-reports.sh"
    echo ""
    exit 1
fi

echo "✓ OpenMRS data directory: ${OPENMRS_DATA_DIR}"

# Verify OpenMRS data directory exists
if [ ! -d "$OPENMRS_DATA_DIR" ]; then
    echo "❌ Error: OpenMRS data directory does not exist: ${OPENMRS_DATA_DIR}"
    echo "Please ensure OpenMRS is properly installed and the data directory path is correct."
    exit 1
fi

echo "✓ OpenMRS data directory exists"

# Define target directory
TARGET_DIR="${OPENMRS_DATA_DIR}/configuration/reports/generic"

# Create directory structure
echo ""
echo "📁 Creating directory structure..."
mkdir -p "${TARGET_DIR}"

echo "✓ Created directory: ${TARGET_DIR}"

# Check if source reports exist
SOURCE_DIR="./reports-generic"
if [ ! -d "$SOURCE_DIR" ]; then
    echo ""
    echo "⚠️  Warning: ./reports-generic directory not found in current location"
    echo "   Looking for generic reports in current directory..."

    # Try to find reports in current directory
    CURRENT_REPORTS=$(ls -1 *-generic.json 2>/dev/null | wc -l)
    if [ "$CURRENT_REPORTS" -gt 0 ]; then
        echo "   Found ${CURRENT_REPORTS} generic reports in current directory"
        echo "   Using current directory instead of ./reports-generic"
        SOURCE_DIR="."
    else
        echo "❌ Error: No generic reports found in ./reports-generic or current directory"
        echo "   Please ensure you're running this script from the correct location."
        exit 1
    fi
fi

echo "✓ Source directory: ${SOURCE_DIR}"

# Count reports to be deployed
REPORT_COUNT=$(ls -1 ${SOURCE_DIR}/*-generic.json 2>/dev/null | wc -l)
echo "📊 Found ${REPORT_COUNT} generic reports to deploy"

if [ "$REPORT_COUNT" -eq 0 ]; then
    echo "❌ Error: No generic reports found in ${SOURCE_DIR}"
    exit 1
fi

# Copy generic reports
echo ""
echo "📋 Copying generic reports to runtime directory..."
cp ${SOURCE_DIR}/*-generic.json "${TARGET_DIR}/"

echo "✓ Copied ${REPORT_COUNT} generic reports"

# Verify reports were copied
DEPLOYED_COUNT=$(ls -1 ${TARGET_DIR}/*-generic.json 2>/dev/null | wc -l)
echo "✓ Verified ${DEPLOYED_COUNT} reports in target directory"

# Set proper permissions
echo ""
echo "🔐 Setting permissions..."
chmod 644 "${TARGET_DIR}"/*-generic.json
echo "✓ Set read permissions for all users"

# Show some example reports
echo ""
echo "📝 Sample deployed reports:"
ls -1 "${TARGET_DIR}"/*-generic.json | head -5 | while read report; do
    basename "$report"
done
echo "... and $((DEPLOYED_COUNT - 5)) more"

# Show categorization summary if possible
echo ""
echo "📊 Report categories summary:"
ANC_COUNT=$(ls -1 "${TARGET_DIR}"/ANC*-generic.json 2>/dev/null | wc -l)
ART_COUNT=$(ls -1 "${TARGET_DIR}"/ART*-generic.json 2>/dev/null | wc -l)
HMIS_COUNT=$(ls -1 "${TARGET_DIR}"/106*generic.json 2>/dev/null | wc -l)
MER_COUNT=$(ls -1 "${TARGET_DIR}"/MER*-generic.json 2>/dev/null | wc -l)

echo "  ANC Reports: ${ANC_COUNT}"
echo "  ART Reports: ${ART_COUNT}"
echo "  HMIS Reports: ${HMIS_COUNT}"
echo "  MER Reports: ${MER_COUNT}"

# Completion message
echo ""
echo "========================================"
echo "✅ Generic reports deployment complete!"
echo "========================================"
echo ""
echo "📁 Deployment location: ${TARGET_DIR}"
echo "📊 Total reports deployed: ${DEPLOYED_COUNT}"
echo ""
echo "🚀 Next steps:"
echo "   1. Restart OpenMRS (or reload the Report Builder module)"
echo "   2. Reports will be automatically imported on module startup"
echo "   3. Check OpenMRS logs for import status"
echo "   4. Reports will be available in the report library"
echo ""
echo "📖 For manual import, use REST API:"
echo "   curl -X POST http://localhost:8080/openmrs/ws/rest/v1/reportbuilder/generic/import"
echo ""
echo "🔍 To check import status:"
echo "   curl -X GET http://localhost:8080/openmrs/ws/rest/v1/reportbuilder/generic/status"
echo ""
