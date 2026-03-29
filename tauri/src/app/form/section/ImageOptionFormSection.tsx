import type { CommandParams } from "../../../model/CommandParam";
import Check from "./element/Check";
import Text from "./element/TextFormElement";

export default function ImageOptionFormSection({
	imageOption,
}: {
	imageOption: CommandParams;
}) {
	const prefix = imageOption.prefix;

	const threshold = imageOption.find("threshold");
	const pixelToleranceLevel = imageOption.find("pixelToleranceLevel");
	const allowingPercentOfDifferentPixels = imageOption.find(
		"allowingPercentOfDifferentPixels",
	);
	const rectangleLineWidth = imageOption.find("rectangleLineWidth");
	const minimalRectangleSize = imageOption.find("minimalRectangleSize");
	const maximalRectangleCount = imageOption.find("maximalRectangleCount");
	const excludedAreas = imageOption.find("excludedAreas");
	const drawExcludedRectangles = imageOption.find("drawExcludedRectangles");
	const fillExcludedRectangles = imageOption.find("fillExcludedRectangles");
	const percentOpacityExcludedRectangles = imageOption.find(
		"percentOpacityExcludedRectangles",
	);
	const excludedRectangleColor = imageOption.find("excludedRectangleColor");
	const fillDifferenceRectangles = imageOption.find("fillDifferenceRectangles");
	const percentOpacityDifferenceRectangles = imageOption.find(
		"percentOpacityDifferenceRectangles",
	);
	const differenceRectangleColor = imageOption.find("differenceRectangleColor");

	return (
		<>
			<Text prefix={prefix} element={threshold} />
			<Text prefix={prefix} element={pixelToleranceLevel} />
			<Text prefix={prefix} element={allowingPercentOfDifferentPixels} />
			<Text prefix={prefix} element={rectangleLineWidth} />
			<Text prefix={prefix} element={minimalRectangleSize} />
			<Text prefix={prefix} element={maximalRectangleCount} />
			<Text prefix={prefix} element={excludedAreas} />
			<Check prefix={prefix} element={drawExcludedRectangles} />
			<Check prefix={prefix} element={fillExcludedRectangles} />
			<Text prefix={prefix} element={percentOpacityExcludedRectangles} />
			<Text prefix={prefix} element={excludedRectangleColor} />
			<Check prefix={prefix} element={fillDifferenceRectangles} />
			<Text prefix={prefix} element={percentOpacityDifferenceRectangles} />
			<Text prefix={prefix} element={differenceRectangleColor} />
		</>
	);
}
