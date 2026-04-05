import type { ImageOption } from "../../../model/SelectParameter";
import Check from "./element/Check";
import PlainText from "./element/PlainText";

export default function ImageOptionFormSection({
	imageOption,
}: {
	imageOption: ImageOption;
}) {
	const prefix = imageOption.prefix;

	return (
		<>
			<PlainText prefix={prefix} element={imageOption.threshold} />
			<PlainText prefix={prefix} element={imageOption.pixelToleranceLevel} />
			<PlainText
				prefix={prefix}
				element={imageOption.allowingPercentOfDifferentPixels}
			/>
			<PlainText prefix={prefix} element={imageOption.rectangleLineWidth} />
			<PlainText prefix={prefix} element={imageOption.minimalRectangleSize} />
			<PlainText prefix={prefix} element={imageOption.maximalRectangleCount} />
			<PlainText prefix={prefix} element={imageOption.excludedAreas} />
			<Check prefix={prefix} element={imageOption.drawExcludedRectangles} />
			<Check prefix={prefix} element={imageOption.fillExcludedRectangles} />
			<PlainText
				prefix={prefix}
				element={imageOption.percentOpacityExcludedRectangles}
			/>
			<PlainText prefix={prefix} element={imageOption.excludedRectangleColor} />
			<Check prefix={prefix} element={imageOption.fillDifferenceRectangles} />
			<PlainText
				prefix={prefix}
				element={imageOption.percentOpacityDifferenceRectangles}
			/>
			<PlainText
				prefix={prefix}
				element={imageOption.differenceRectangleColor}
			/>
		</>
	);
}
