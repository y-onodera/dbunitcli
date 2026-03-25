import type { CommandParams } from "../../../model/CommandParam";
import Check from "./Check";
import Text from "./TextFormElement";

export default function ImageOptionFormSection({
	imageOption,
}: {
	imageOption: CommandParams;
}) {
	const find = (name: string) =>
		imageOption.elements.find((e) => e.name === name);
	const prefix = imageOption.prefix;

	const threshold = find("threshold");
	const pixelToleranceLevel = find("pixelToleranceLevel");
	const allowingPercentOfDifferentPixels = find(
		"allowingPercentOfDifferentPixels",
	);
	const rectangleLineWidth = find("rectangleLineWidth");
	const minimalRectangleSize = find("minimalRectangleSize");
	const maximalRectangleCount = find("maximalRectangleCount");
	const excludedAreas = find("excludedAreas");
	const drawExcludedRectangles = find("drawExcludedRectangles");
	const fillExcludedRectangles = find("fillExcludedRectangles");
	const percentOpacityExcludedRectangles = find(
		"percentOpacityExcludedRectangles",
	);
	const excludedRectangleColor = find("excludedRectangleColor");
	const fillDifferenceRectangles = find("fillDifferenceRectangles");
	const percentOpacityDifferenceRectangles = find(
		"percentOpacityDifferenceRectangles",
	);
	const differenceRectangleColor = find("differenceRectangleColor");

	return (
		<>
			{threshold && <Text prefix={prefix} element={threshold} />}
			{pixelToleranceLevel && (
				<Text prefix={prefix} element={pixelToleranceLevel} />
			)}
			{allowingPercentOfDifferentPixels && (
				<Text prefix={prefix} element={allowingPercentOfDifferentPixels} />
			)}
			{rectangleLineWidth && (
				<Text prefix={prefix} element={rectangleLineWidth} />
			)}
			{minimalRectangleSize && (
				<Text prefix={prefix} element={minimalRectangleSize} />
			)}
			{maximalRectangleCount && (
				<Text prefix={prefix} element={maximalRectangleCount} />
			)}
			{excludedAreas && <Text prefix={prefix} element={excludedAreas} />}
			{drawExcludedRectangles && (
				<Check prefix={prefix} element={drawExcludedRectangles} />
			)}
			{fillExcludedRectangles && (
				<Check prefix={prefix} element={fillExcludedRectangles} />
			)}
			{percentOpacityExcludedRectangles && (
				<Text prefix={prefix} element={percentOpacityExcludedRectangles} />
			)}
			{excludedRectangleColor && (
				<Text prefix={prefix} element={excludedRectangleColor} />
			)}
			{fillDifferenceRectangles && (
				<Check prefix={prefix} element={fillDifferenceRectangles} />
			)}
			{percentOpacityDifferenceRectangles && (
				<Text prefix={prefix} element={percentOpacityDifferenceRectangles} />
			)}
			{differenceRectangleColor && (
				<Text prefix={prefix} element={differenceRectangleColor} />
			)}
		</>
	);
}
