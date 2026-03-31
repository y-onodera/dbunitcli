import type { ImageOption } from "../../../model/CommandParam";
import Check from "./element/Check";
import Text from "./element/TextFormElement";

export default function ImageOptionFormSection({
	imageOption,
}: {
	imageOption: ImageOption;
}) {
	const prefix = imageOption.prefix;

	return (
		<>
			<Text prefix={prefix} element={imageOption.threshold} />
			<Text prefix={prefix} element={imageOption.pixelToleranceLevel} />
			<Text prefix={prefix} element={imageOption.allowingPercentOfDifferentPixels} />
			<Text prefix={prefix} element={imageOption.rectangleLineWidth} />
			<Text prefix={prefix} element={imageOption.minimalRectangleSize} />
			<Text prefix={prefix} element={imageOption.maximalRectangleCount} />
			<Text prefix={prefix} element={imageOption.excludedAreas} />
			<Check prefix={prefix} element={imageOption.drawExcludedRectangles} />
			<Check prefix={prefix} element={imageOption.fillExcludedRectangles} />
			<Text prefix={prefix} element={imageOption.percentOpacityExcludedRectangles} />
			<Text prefix={prefix} element={imageOption.excludedRectangleColor} />
			<Check prefix={prefix} element={imageOption.fillDifferenceRectangles} />
			<Text prefix={prefix} element={imageOption.percentOpacityDifferenceRectangles} />
			<Text prefix={prefix} element={imageOption.differenceRectangleColor} />
		</>
	);
}
